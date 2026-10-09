package com.shuttleup.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordConfigTest {

    private final PasswordEncoder encoder = new PasswordConfig().passwordEncoder();

    @Test
    void 同じパスワードでも異なるハッシュになり両方とも照合できる() {
        String rawPassword = "test-password";

        String firstHash = encoder.encode(rawPassword);
        String secondHash = encoder.encode(rawPassword);

        assertThat(firstHash).isNotEqualTo(rawPassword).isNotEqualTo(secondHash);
        assertThat(encoder.matches(rawPassword, firstHash)).isTrue();
        assertThat(encoder.matches(rawPassword, secondHash)).isTrue();
        assertThat(encoder.matches("wrong-password", firstHash)).isFalse();
    }
}
