package com.practice.nexora.user;

import com.practice.nexora.common.NexoraProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final NexoraProperties.Admin admin;

    @Autowired
    AdminAccountInitializer(UserAccountRepository accounts, PasswordEncoder passwordEncoder, NexoraProperties properties) {
        this(accounts, passwordEncoder, properties.admin());
    }

    AdminAccountInitializer(UserAccountRepository accounts, PasswordEncoder passwordEncoder, NexoraProperties.Admin admin) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.admin = admin;
    }

    @Override
    public void run(ApplicationArguments args) {
        createAdminIfMissing();
    }

    void createAdminIfMissing() {
        if (!admin.isConfigured()) {
            log.info("No admin account configured (ADMIN_EMAIL / ADMIN_PASSWORD are empty");
            return;
        }
        String normalized = UserAccount.normalizeEmail(admin.email());
        if (accounts.existsByEmail(normalized)) {
            log.info("Admin account {} already exists", normalized);
            return;
        }
        accounts.save(new UserAccount(normalized, passwordEncoder.encode(admin.password()), Role.ADMIN));
        log.info("Created admin account {}", normalized);
    }
}
