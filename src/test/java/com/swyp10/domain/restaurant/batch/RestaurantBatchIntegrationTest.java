package com.swyp10.domain.restaurant.batch;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.restaurant.entity.Restaurant;
import com.swyp10.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestConfig.class, RestaurantBatchTestConfig.class})
@ActiveProfiles("test")
@DisplayName("RestaurantBatch 통합 테스트")
@EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
public class RestaurantBatchIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job restaurantSyncJob;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    @DisplayName("RestaurantBatchConfig 통합 테스트 - 배치 실행 및 데이터 저장 검증")
    void testRestaurantBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(restaurantSyncJob, jobParameters);

        // 잡 정상 종료 확인
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // DB에 레스토랑 데이터 최소 1건 이상 저장 확인
        List<Restaurant> restaurants = restaurantRepository.findAll();
        assertThat(restaurants).isNotEmpty();

        // 저장된 첫 데이터 필드 검증
        Restaurant first = restaurants.get(0);
        assertThat(first.getContentId()).isNotNull();
        assertThat(first.getBasicInfo().getTitle()).isNotNull();
    }
}
