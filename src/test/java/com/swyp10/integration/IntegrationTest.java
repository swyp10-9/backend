package com.swyp10.integration;

import com.swyp10.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 통합 테스트를 위한 메타 어노테이션
 *
 * 이 어노테이션을 사용하면 다음 설정들이 자동으로 적용됩니다:
 * - @SpringBootTest: 전체 Spring Boot 애플리케이션 컨텍스트 로드
 * - @ActiveProfiles("test"): 테스트 프로필 활성화
 * - @Transactional: 각 테스트 후 롤백 (데이터베이스 초기화)
 * - @Import(TestConfig.class): 테스트 전용 설정 적용
 *
 * 사용 예시:
 * @IntegrationTest
 * class MyServiceIntegrationTest {
 *     // 테스트 코드
 * }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
//        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
public @interface IntegrationTest {
}
