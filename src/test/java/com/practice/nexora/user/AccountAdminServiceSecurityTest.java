package com.practice.nexora.user;

import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AccountAdminServiceSecurityTest {

    @Autowired
    private AccountAdminService service;

    @Test
    @WithMockUser(roles = "USER")
    void aUserCannotCallTheServiceEvenWithoutAnyUrl() {
        assertThatThrownBy(() -> service.listAccounts()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void anAdminCan() {
        assertThatCode(() -> service.listAccounts()).doesNotThrowAnyException();
    }
}