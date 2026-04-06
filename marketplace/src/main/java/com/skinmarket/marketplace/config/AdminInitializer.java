package com.skinmarket.marketplace.config;

import com.skinmarket.marketplace.entity.User;
import com.skinmarket.marketplace.enums.UserRole;
import com.skinmarket.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;


    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User admin = new User(
                    UUID.randomUUID(),
                    adminUsername,
                    passwordEncoder.encode(adminPassword),
                    BigDecimal.ZERO,
                    UserRole.ADMIN,
                    10000L
            );
            userRepository.createUser(admin);
        }
    }
}