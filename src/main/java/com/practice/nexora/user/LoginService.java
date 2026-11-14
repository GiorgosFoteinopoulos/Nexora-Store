package com.practice.nexora.user;


import com.practice.nexora.common.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginService {

    static final String WRONG_LOGIN = "Wrong email or password";

    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public LoginService(UserAccountRepository accounts, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    public TokenResponse login(LoginRequest request) {
        String email = UserAccount.normalizeEmail(request.email());
        UserAccount account = accounts.findByEmail(email)
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException(WRONG_LOGIN));
        return tokens.issueFor(account);
    }
}
