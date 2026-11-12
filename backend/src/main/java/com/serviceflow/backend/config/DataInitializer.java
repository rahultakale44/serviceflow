package com.serviceflow.backend.config;

import com.serviceflow.backend.entity.User;
import com.serviceflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data initialization for default admin user.
 * Creates a default admin user if no users exist in the database.
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Create default admin user if no users exist
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@serviceflow.com");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setRole(User.Role.ADMIN);
                admin.setActive(true);

                userRepository.save(admin);
                
                logger.info("================================");
                logger.info("Default admin user created");
                logger.info("Username: admin");
                logger.info("Password: Admin@123");
                logger.info("PLEASE CHANGE THIS PASSWORD IMMEDIATELY!");
                logger.info("================================");
            } else {
                logger.info("Users already exist. Skipping default admin creation.");
            }
        };
    }
}
