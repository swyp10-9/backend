package com.swyp10.domain.region.batch;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.region.entity.AreaCode;
import com.swyp10.domain.region.entity.LdongCode;
import com.swyp10.domain.region.repository.AreaCodeRepository;
import com.swyp10.domain.region.repository.LdongCodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
@Import({TestConfig.class, RegionBatchTestConfig.class})
@ActiveProfiles("test")
@DisplayName("RegionBatch 통합 테스트")
@EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
public class RegionBatchIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job regionSyncJob;

    @Autowired
    private AreaCodeRepository areaCodeRepository;

    @Autowired
    private LdongCodeRepository ldongCodeRepository;

    @Test
    @DisplayName("RegionBatchConfig 통합 테스트 - 배치 실행 및 데이터 저장 검증")
    public void testRegionBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(regionSyncJob, jobParameters);

        // 배치 잡 정상 종료 확인
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // AreaCode 데이터 저장 여부 확인 (최소 1건 이상)
        List<AreaCode> areaCodes = areaCodeRepository.findAll();
        assertThat(areaCodes).isNotEmpty();
        System.out.println("AreaCode 저장 개수: " + areaCodes.size());

        // LdongCode 데이터 저장 여부 확인 (최소 1건 이상)
        List<LdongCode> ldongCodes = ldongCodeRepository.findAll();
        assertThat(ldongCodes).isNotEmpty();
        System.out.println("LdongCode 저장 개수: " + ldongCodes.size());
    }
}
