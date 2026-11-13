package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.LoginRequestDto;
import com.serviceflow.backend.dto.LoginResponseDto;
import com.serviceflow.backend.dto.RegisterRequestDto;
import com.serviceflow.backend.dto.UserResponseDto;
import com.serviceflow.backend.entity.User;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.repository.UserRepository;
import com.serviceflow.backend.security.JwtTokenProvider;
import com.serviceflow.backend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService.
 * Covers login, registration, privilege escalation prevention,
 * and duplicate detection.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john");
        testUser.setEmail("john@example.com");
        testUser.setPassword("hashed_password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(User.Role.CUSTOMER_SERVICE);
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        testPrincipal = UserPrincipal.create(testUser);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_returnsTokenAndUser() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("jwt.token.here");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        // getPrincipal() is only called inside generateToken(auth) which is mocked —
        // do NOT stub it here to avoid UnnecessaryStubbingException.

        LoginResponseDto result = authenticationService.login(new LoginRequestDto("john", "password"));

        assertNotNull(result);
        assertEquals("jwt.token.here", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertNotNull(result.getUser());
        assertEquals("john", result.getUser().getUsername());
        assertEquals(User.Role.CUSTOMER_SERVICE, result.getUser().getRole());
        verify(userRepository).save(any(User.class)); // last login updated
    }

    @Test
    void login_withInvalidCredentials_throwsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authenticationService.login(new LoginRequestDto("john", "wrong")));
        verify(userRepository, never()).save(any());
    }

    // ── Registration ──────────────────────────────────────────────────────────

    @Test
    void register_withNewUser_createsAndReturnsUser() {
        RegisterRequestDto request = new RegisterRequestDto(
                "newuser", "new@example.com", "Password@1",
                "New", "User", User.Role.TECHNICIAN);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@1")).thenReturn("hashed");

        User saved = new User();
        saved.setId(2L);
        saved.setUsername("newuser");
        saved.setEmail("new@example.com");
        saved.setPassword("hashed");
        saved.setFirstName("New");
        saved.setLastName("User");
        saved.setRole(User.Role.TECHNICIAN);
        saved.setActive(true);
        saved.setCreatedAt(LocalDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponseDto result = authenticationService.register(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals(User.Role.TECHNICIAN, result.getRole());
        verify(passwordEncoder).encode("Password@1");
        verify(userRepository).save(argThat(u -> u.getRole() == User.Role.TECHNICIAN));
    }

    @Test
    void register_withDuplicateUsername_throwsDuplicateResourceException() {
        RegisterRequestDto request = new RegisterRequestDto(
                "john", "other@example.com", "Password@1",
                "Other", "User", User.Role.CUSTOMER_SERVICE);

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withDuplicateEmail_throwsDuplicateResourceException() {
        RegisterRequestDto request = new RegisterRequestDto(
                "otheruser", "john@example.com", "Password@1",
                "Other", "User", User.Role.CUSTOMER_SERVICE);

        when(userRepository.existsByUsername("otheruser")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }

    // ── Privilege escalation prevention ───────────────────────────────────────

    @Test
    void register_withAdminRole_persistsExactRoleAsAssignedByAdmin() {
        // The @PreAuthorize("hasRole('ADMIN')") gate on the controller prevents
        // non-admins from reaching this method. Here we verify that whatever role
        // the (authenticated admin) caller supplies is stored unchanged — no
        // server-side down-grade or up-grade occurs silently.
        RegisterRequestDto adminRequest = new RegisterRequestDto(
                "newadmin", "newadmin@example.com", "Password@1",
                "New", "Admin", User.Role.ADMIN);

        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(userRepository.existsByEmail("newadmin@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        User saved = new User();
        saved.setId(3L);
        saved.setUsername("newadmin");
        saved.setEmail("newadmin@example.com");
        saved.setPassword("hashed");
        saved.setFirstName("New");
        saved.setLastName("Admin");
        saved.setRole(User.Role.ADMIN);
        saved.setActive(true);
        saved.setCreatedAt(LocalDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponseDto result = authenticationService.register(adminRequest);

        // Role must be exactly what the admin specified
        assertEquals(User.Role.ADMIN, result.getRole());
        verify(userRepository).save(argThat(u -> u.getRole() == User.Role.ADMIN));
    }

    @Test
    void register_passwordIsStoredHashed_notPlaintext() {
        RegisterRequestDto request = new RegisterRequestDto(
                "secureuser", "secure@example.com", "PlainPassword@1",
                "Secure", "User", User.Role.TECHNICIAN);

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("PlainPassword@1")).thenReturn("$2a$12$hashed");

        User saved = new User();
        saved.setId(4L);
        saved.setUsername("secureuser");
        saved.setEmail("secure@example.com");
        saved.setPassword("$2a$12$hashed");
        saved.setFirstName("Secure");
        saved.setLastName("User");
        saved.setRole(User.Role.TECHNICIAN);
        saved.setActive(true);
        saved.setCreatedAt(LocalDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        authenticationService.register(request);

        // Verify save was called with hashed password, never the plain one
        verify(userRepository).save(argThat(u ->
                u.getPassword().equals("$2a$12$hashed") &&
                !u.getPassword().equals("PlainPassword@1")));
    }

    // ── getCurrentUser ─────────────────────────────────────────────────────────

    @Test
    void getCurrentUser_withAuthenticatedUser_returnsUserDto() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testPrincipal, null, testPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponseDto result = authenticationService.getCurrentUser();

        assertNotNull(result);
        assertEquals("john", result.getUsername());
        assertEquals(User.Role.CUSTOMER_SERVICE, result.getRole());

        SecurityContextHolder.clearContext();
    }
}
