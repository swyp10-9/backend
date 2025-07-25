package com.swyp10.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@ActiveProfiles("test")
@Import(TestConfig.class)
class JwtConfigTest {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Test
    void jwtSecretShouldBeLoadedFromTestConfig() {
        System.out.println("JWT Secret length: " + jwtSecret.length());
        System.out.println("JWT Secret starts with: " + jwtSecret.substring(0, Math.min(20, jwtSecret.length())) + "...");
        
        // 최소 32바이트(256비트) 확인
        assertThat(jwtSecret.length()).isGreaterThanOrEqualTo(32);
        assertThat(jwtSecret).startsWith("abcdefgh");
    }
}
