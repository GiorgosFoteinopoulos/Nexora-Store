package com.practice.nexora.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAccountInitializerTest {

    @Mock
    private UserAccountRepository accounts;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminAccountInitializer initializer(String email, String password) {
        return new AdminAccountInitializer(accounts, passwordEncoder, email, password);
    }

    @Test
    void createsTheAdminWhenMissing() {
        when(passwordEncoder.encode("a-long-admin-password")).thenReturn("HASH");

        initializer("Admin@Nexora.local", "a-long-admin-password").createAdminIfMissing();

        ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(accounts).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("admin@nexora.local");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("HASH");
    }

    @Test
    void doesNothingWhenTheAdminAlreadyExists() {
        when(accounts.existsByEmail("admin@nexora.local")).thenReturn(true);

        initializer("admin@nexora.local", "a-long-admin-password").createAdminIfMissing();

        verify(accounts, never()).save(any());
    }

    @Test
    void createsNoAdminWhenNothingIsConfigured() {
        initializer("", "").createAdminIfMissing();

        verifyNoInteractions(accounts, passwordEncoder);
    }
}
