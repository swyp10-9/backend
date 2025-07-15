package com.swyp10.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 테스트 전용 설정
 * 통합 테스트에서 필요한 Bean들을 정의합니다.
 */
@TestConfiguration
public class TestConfig {

    /**
     * 테스트용 비밀번호 인코더
     * 메인 설정의 인코더보다 우선적으로 사용됩니다.
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
