package com.serviceflow.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceflow.backend.dto.LoginRequestDto;
import com.serviceflow.backend.dto.RegisterRequestDto;
import com.serviceflow.backend.entity.User;
import com.serviceflow.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the security layer.
 *
 * Covers:
 * - Successful login returns JWT
 * - Invalid credentials return 401
 * - Accessing protected APIs without token returns 401
 * - Accessing protected APIs with invalid token returns 401
 * - Authenticated access succeeds (200)
 * - Registration requires ADMIN role (401 without token, 403 for non-admin)
 * - Role-based access restrictions enforced (privilege escalation prevented)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    // Construct directly — no auto-registered ObjectMapper in MOCK web context
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private MockMvc mockMvc;

    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String ME_URL = "/api/v1/auth/me";
    private static final String CUSTOMERS_URL = "/api/v1/customers";

    private String adminToken;
    private String techToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Remove stale test users (but keep the testadmin created by DataInitializer)
        userRepository.findByUsername("testtech").ifPresent(userRepository::delete);
        userRepository.findByUsername("newmanager").ifPresent(userRepository::delete);

        User tech = new User();
        tech.setUsername("testtech");
        tech.setEmail("testtech@test.com");
        tech.setPassword(passwordEncoder.encode("Tech@1234"));
        tech.setFirstName("Tech");
        tech.setLastName("User");
        tech.setRole(User.Role.TECHNICIAN);
        tech.setActive(true);
        userRepository.save(tech);

        // Generate tokens directly (avoids needing to perform HTTP login for setup)
        adminToken = tokenProvider.generateToken(
                userRepository.findByUsername("testadmin")
                        .map(User::getId).orElse(0L),
                "testadmin",
                User.Role.ADMIN.name()
        );
        techToken = tokenProvider.generateToken(
                tech.getId(),
                "testtech",
                User.Role.TECHNICIAN.name()
        );
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_returns200AndToken() throws Exception {
        LoginRequestDto request = new LoginRequestDto("testadmin", "TestAdmin@123");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.username", is("testadmin")))
                .andExpect(jsonPath("$.user.role", is("ADMIN")))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        LoginRequestDto request = new LoginRequestDto("testadmin", "wrongpassword");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withNonexistentUser_returns401() throws Exception {
        LoginRequestDto request = new LoginRequestDto("nobody", "password");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withMissingUsername_returns400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"somepassword\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── Unauthenticated access ─────────────────────────────────────────────────

    @Test
    void meEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(ME_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customersEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(CUSTOMERS_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpoint_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get(ME_URL)
                        .header("Authorization", "Bearer this.is.not.valid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpoint_withMalformedBearerHeader_returns401() throws Exception {
        mockMvc.perform(get(ME_URL)
                        .header("Authorization", "NotBearer abc123"))
                .andExpect(status().isUnauthorized());
    }

    // ── Authenticated access ───────────────────────────────────────────────────

    @Test
    void getCurrentUser_withValidAdminToken_returns200AndUserInfo() throws Exception {
        mockMvc.perform(get(ME_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testadmin")))
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void customersEndpoint_withValidToken_returns200() throws Exception {
        mockMvc.perform(get(CUSTOMERS_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ── Role-based access — registration ──────────────────────────────────────

    @Test
    void register_withAdminToken_returns201() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "newmanager", "newmanager@test.com", "Manager@1234",
                "New", "Manager", User.Role.MANAGER);

        mockMvc.perform(post(REGISTER_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("newmanager")))
                .andExpect(jsonPath("$.role", is("MANAGER")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_withoutToken_returns401() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "attacker", "attacker@test.com", "Attack@1234",
                "Bad", "Actor", User.Role.ADMIN);

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withTechnicianToken_returns403() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "sneakyuser", "sneaky@test.com", "Sneaky@1234",
                "Sneaky", "User", User.Role.CUSTOMER_SERVICE);

        mockMvc.perform(post(REGISTER_URL)
                        .header("Authorization", "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_privilegeEscalation_technicianCannotCreateAdmin() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "escalated", "escalated@test.com", "Escalate@1234",
                "Escalated", "Admin", User.Role.ADMIN);

        mockMvc.perform(post(REGISTER_URL)
                        .header("Authorization", "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
