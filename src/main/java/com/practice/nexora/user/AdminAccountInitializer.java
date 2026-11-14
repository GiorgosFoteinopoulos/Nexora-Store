package com.practice.nexora.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    AdminAccountInitializer(UserAccountRepository accounts, PasswordEncoder passwordEncoder, @Value("${nexora.admin.email:}") String email, @Value("${nexora.admin.password:}") String password) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        createAdminIfMissing();
    }

    void createAdminIfMissing() {
        if (email.isBlank() || password.isBlank()) {
            log.info("No admin account configured (ADMIN_EMAIL / ADMIN_PASSWORD are empty");
            return;
        }
        String normalized = UserAccount.normalizeEmail(email);
        if (accounts.existsByEmail(normalized)) {
            log.info("Admin account {} already exists", normalized);
            return;
        }
        accounts.save(new UserAccount(normalized, passwordEncoder.encode(password), Role.ADMIN));
        log.info("Created admin account {}", normalized);
    }
}
