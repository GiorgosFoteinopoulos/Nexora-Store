package com.practice.nexora.user;

import com.practice.nexora.common.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserAccountRepository accounts;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService service;

    @Test
    void storesTheHashNeverThePassword() {
        when(passwordEncoder.encode("correct-horse")).thenReturn("HASHED");
        when(accounts.save(any())).thenAnswer(call -> call.getArgument(0));

        UserResponse response = service.register(new RegisterRequest("george@example.com", "correct-horse"));

        ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(accounts).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("HASHED");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(response).isEqualTo(new UserResponse("george@example.com", Role.USER));
    }

    @Test
    void checksForDuplicatesWithTheCleanedEmail() {
        when(accounts.existsByEmail("george@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest(" George@Example.com ", "correct-horse")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void aDuplicateIsNeverHashedOrSaved() {
        when(accounts.existsByEmail("george@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest("george@example.com", "correct-horse")))
                .isInstanceOf(ConflictException.class);
        verify(passwordEncoder, never()).encode(any());
        verify(accounts, never()).save(any());
    }
}
