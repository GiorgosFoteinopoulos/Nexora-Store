package com.practice.nexora.user;


import com.practice.nexora.common.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrationService {

    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserAccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        String email = UserAccount.normalizeEmail(request.email());
        if (accounts.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        String hash = passwordEncoder.encode(request.password());
        UserAccount saved = accounts.save(new UserAccount(email, hash, Role.USER));
        return UserResponse.from(saved);
    }
}
