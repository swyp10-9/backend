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
        if (waitForBatchSchemaReady()) {
            runFestivalSyncJob("startup");
        } else {
            log.error("[Batch] Batch 스키마가 준비되지 않아 startup 배치를 건너뜁니다.");
        }
    }

    private boolean waitForBatchSchemaReady() {
        int maxRetries = 10;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try (Connection conn = dataSource.getConnection()) {
                // BATCH_JOB_INSTANCE 테이블 존재 확인
                conn.prepareStatement("SELECT 1 FROM BATCH_JOB_INSTANCE LIMIT 1").executeQuery();
                log.info("[Batch] Batch 스키마 준비 완료");
                return true;
            } catch (Exception e) {
                log.info("[Batch] Batch 스키마 준비 대기 중... ({}/{}) - {}",
                    retryCount + 1, maxRetries, e.getMessage());
                try {
                    Thread.sleep(3000); // 3초 대기
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("[Batch] 대기 중 인터럽트 발생", ie);
                    return false;
                }
                retryCount++;
            }
        }

        log.error("[Batch] Batch 스키마가 {}초 내에 준비되지 않았습니다.", maxRetries * 3);
        return false;
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
