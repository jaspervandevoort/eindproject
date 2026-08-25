package be.ucll.backend.eindproject.config;

import be.ucll.backend.eindproject.model.Role;
import be.ucll.backend.eindproject.model.User;
import be.ucll.backend.eindproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == Role.ADMIN);

        if (!adminExists) {
            String generatedPassword = generateSecurePassword();
            String hashedPassword = passwordEncoder.encode(generatedPassword);

            User admin = new User("Admin", "admin@test.com", hashedPassword);
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            logger.warn("Email: admin@test.com");
            logger.warn("Admin Password: {}", generatedPassword);

        }
    }

    private String generateSecurePassword() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
