package com.swyp10.domain.festival.batch;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
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
@Import({TestConfig.class, FestivalBatchTestConfig.class})
@ActiveProfiles("test")
@DisplayName("FestivalBatch 통합 테스트")
@EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
public class FestivalBatchIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job festivalSyncJob;

    @Autowired
    private FestivalRepository festivalRepository;

    @Test
    @DisplayName("FestivalBatchConfig 통합 테스트 - 잡 실행 및 데이터 저장 검증")
    void testFestivalBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(festivalSyncJob, jobParameters);

        // 잡이 정상 완료되었는지 검증
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // DB에 축제 데이터가 저장되었는지 최소 1건 이상 확인
        List<Festival> festivals = festivalRepository.findAll();
        assertThat(festivals).isNotEmpty();

        // 저장된 데이터 중 첫 번째 축제의 필드 검증(예: contentId, 제목)
        Festival firstFestival = festivals.get(0);
        assertThat(firstFestival.getContentId()).isNotNull();
        assertThat(firstFestival.getBasicInfo().getTitle()).isNotNull();
    }
}
