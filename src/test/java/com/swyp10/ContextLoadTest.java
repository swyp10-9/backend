package com.swyp10;

import com.swyp10.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestConfig.class)
class ContextLoadTest {

    @Test
    void contextLoads() {
        // Spring Context가 정상적으로 로딩되는지 확인
    }
}
