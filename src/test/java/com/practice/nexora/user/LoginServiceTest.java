package com.practice.nexora.user;

import com.practice.nexora.common.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserAccountRepository accounts;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokens;

    @InjectMocks
    private LoginService service;

    private final UserAccount george = new UserAccount("george@example.com", "HASH", Role.USER);

    @Test
    void theRightPasswordGetsAToken() {
        TokenResponse token = new TokenResponse("a.b.c", "Bearer", 3600);
        when(accounts.findByEmail("george@example.com")).thenReturn(Optional.of(george));
        when(passwordEncoder.matches("correct-horse", "HASH")).thenReturn(true);
        when(tokens.issueFor(george)).thenReturn(token);

        assertThat(service.login(new LoginRequest("George@Example.com ", "correct-horse"))).isEqualTo(token);
    }

    @Test
    void aWrongPasswordIsRefused() {
        when(accounts.findByEmail("george@example.com")).thenReturn(Optional.of(george));
        when(passwordEncoder.matches("wrong-horse", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("george@example.com", "wrong-horse")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(LoginService.WRONG_LOGIN);
        verifyNoInteractions(tokens);
    }

    @Test
    void anUnknownEmailGetsTheSameAnswer() {
        when(accounts.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("nobody@example.com", "correct-horse")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(LoginService.WRONG_LOGIN);
        verifyNoInteractions(tokens);
    }
}
