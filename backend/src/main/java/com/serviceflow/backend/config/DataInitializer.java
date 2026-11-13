package com.serviceflow.backend.config;

import com.serviceflow.backend.entity.User;
import com.serviceflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * Data initialization for default admin user.
 * Creates a default admin user if no users exist in the database.
 * 
 * SECURITY: Admin credentials are read from environment variables.
 * Never hardcode or log passwords.
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@serviceflow.com}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.firstName:System}")
    private String adminFirstName;

    @Value("${app.admin.lastName:Administrator}")
    private String adminLastName;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Create default admin user if no users exist
            if (userRepository.count() == 0) {
                if (!StringUtils.hasText(adminPassword)) {
                    logger.error("========================================");
                    logger.error("FATAL: No admin password configured!");
                    logger.error("Set environment variable: APP_ADMIN_PASSWORD");
                    logger.error("Application cannot start without admin user.");
                    logger.error("========================================");
                    throw new IllegalStateException("Admin password must be configured via APP_ADMIN_PASSWORD environment variable");
                }

                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setFirstName(adminFirstName);
                admin.setLastName(adminLastName);
                admin.setRole(User.Role.ADMIN);
                admin.setActive(true);

                userRepository.save(admin);
                
                logger.info("========================================");
                logger.info("Default admin user created successfully");
                logger.info("Username: {}", adminUsername);
                logger.info("Check environment variables for credentials");
                logger.info("========================================");
            } else {
                logger.info("Users already exist. Skipping default admin creation.");
            }
        };
    }
}
