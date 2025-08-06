package com.swyp10.batch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@RequiredArgsConstructor
@Component
public class FestivalBatchAutoRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job festivalSyncJob;
    private final DataSource dataSource;

    // 서버 시작시 1회 실행
    @Override
    public void run(ApplicationArguments args) throws Exception {
        waitForSchemaReady();
        runFestivalSyncJob("startup");
    }

    private void waitForSchemaReady() throws Exception {
        int maxRetries = 10;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try (Connection conn = dataSource.getConnection()) {
                conn.prepareStatement("SELECT 1 FROM BATCH_JOB_INSTANCE LIMIT 1").executeQuery();
                log.info("[Batch] 스키마 준비 완료");
                return;
            } catch (Exception e) {
                log.info("[Batch] 스키마 준비 대기 중... ({}/{})", retryCount + 1, maxRetries);
                Thread.sleep(2000);
                retryCount++;
            }
        }
        throw new RuntimeException("배치 스키마가 준비되지 않았습니다.");
    }

    // 매일 새벽 3시 자동 실행 (cron: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledFestivalSyncJob() {
        runFestivalSyncJob("scheduled");
    }

    private void runFestivalSyncJob(String triggerType) {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("triggerType", triggerType)
            .toJobParameters();
        try {
            log.info("[Batch] {} 트리거: festivalSyncJob 실행 시작", triggerType);
            jobLauncher.run(festivalSyncJob, params);
            log.info("[Batch] {} 트리거: festivalSyncJob 실행 완료", triggerType);
        } catch (Exception e) {
            log.error("[Batch] {} 트리거: festivalSyncJob 실행 실패", triggerType, e);
        }
    }
}
