package com.practice.nexora.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHashingTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void theSamePasswordGivesADifferentHashEachTime() {
        String first = encoder.encode("correct-horse");
        String second = encoder.encode("correct-horse");

        assertThat(first).isNotEqualTo(second).startsWith("$2a$");
        assertThat(first).doesNotContain("correct-horse");
    }

    @Test
    void aHashCanCheckAPasswordButNotRevealIt() {
        String hash = encoder.encode("correct-horse");

        assertThat(encoder.matches("correct-horse", hash)).isTrue();
        assertThat(encoder.matches("wrong-horse", hash)).isFalse();
    }
}
