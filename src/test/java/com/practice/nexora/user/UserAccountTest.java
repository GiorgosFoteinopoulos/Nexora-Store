package com.practice.nexora.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccountTest {

    @Test
    void theEmailIsStoredTrimmedAndInLowerCase() {
        UserAccount account = new UserAccount(" George@Example.COM", "some-hash", Role.USER);

        assertThat(account.getEmail()).isEqualTo("george@example.com");
    }

    @Test
    void aBlankEmailIsRefused() {
        assertThatThrownBy(() -> new UserAccount("  ", "some-hash", Role.USER))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
