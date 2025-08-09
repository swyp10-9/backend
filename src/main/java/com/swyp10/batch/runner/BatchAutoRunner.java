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
public class BatchAutoRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job festivalSyncJob;
    private final Job restaurantSyncJob;
    private final DataSource dataSource;

    // 서버 시작시 1회 실행
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (waitForBatchSchemaReady()) {
            log.info("[Batch] 서버 시작 시 모든 배치 작업 실행");
            runAllSyncJobs("startup");
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

    // 매일 새벽 3시 자동 실행 - 모든 배치 작업
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledAllSyncJobs() {
        log.info("[Batch] 스케줄러: 모든 배치 작업 실행 시작");
        runAllSyncJobs("scheduled");
    }

    // 매주 일요일 새벽 4시 Festival 배치만 실행 (추가 동기화)
    @Scheduled(cron = "0 0 4 * * SUN")
    public void scheduledFestivalSyncJob() {
        log.info("[Batch] 스케줄러: Festival 배치 작업 실행");
        runFestivalSyncJob("scheduled-weekly");
    }

    // 매주 월요일 새벽 4시 Restaurant 배치만 실행 (추가 동기화)
    @Scheduled(cron = "0 0 4 * * MON")
    public void scheduledRestaurantSyncJob() {
        log.info("[Batch] 스케줄러: Restaurant 배치 작업 실행");
        runRestaurantSyncJob("scheduled-weekly");
    }

    /**
     * 모든 배치 작업 순차 실행
     */
    private void runAllSyncJobs(String triggerType) {
        boolean festivalSuccess = runFestivalSyncJob(triggerType);

        if (festivalSuccess) {
            // Festival 배치 성공 시에만 Restaurant 배치 실행
            log.info("[Batch] Festival 배치 완료, Restaurant 배치 시작");
            runRestaurantSyncJob(triggerType);
        } else {
            log.warn("[Batch] Festival 배치 실패로 인해 Restaurant 배치를 건너뜁니다.");
        }
    }

    /**
     * Festival 배치 실행
     */
    private boolean runFestivalSyncJob(String triggerType) {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("triggerType", triggerType)
            .addString("jobType", "festival")
            .toJobParameters();

        try {
            log.info("[Batch] {} 트리거: festivalSyncJob 실행 시작", triggerType);
            jobLauncher.run(festivalSyncJob, params);
            log.info("[Batch] {} 트리거: festivalSyncJob 실행 완료", triggerType);
            return true;

        } catch (Exception e) {
            log.error("[Batch] {} 트리거: festivalSyncJob 실행 실패", triggerType, e);
            return false;
        }
    }

    /**
     * Restaurant 배치 실행
     */
    private boolean runRestaurantSyncJob(String triggerType) {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("triggerType", triggerType)
            .addString("jobType", "restaurant")
            .toJobParameters();

        try {
            log.info("[Batch] {} 트리거: restaurantSyncJob 실행 시작", triggerType);
            jobLauncher.run(restaurantSyncJob, params);
            log.info("[Batch] {} 트리거: restaurantSyncJob 실행 완료", triggerType);
            return true;

        } catch (Exception e) {
            log.error("[Batch] {} 트리거: restaurantSyncJob 실행 실패", triggerType, e);
            return false;
        }
    }

    /**
     * 병렬 실행 버전
     */
    private void runAllSyncJobsParallel(String triggerType) {
        log.info("[Batch] {} 트리거: 모든 배치 작업 병렬 실행 시작", triggerType);

        // Festival과 Restaurant 배치를 동시에 실행
        Thread festivalThread = new Thread(() -> runFestivalSyncJob(triggerType + "-parallel"));
        Thread restaurantThread = new Thread(() -> runRestaurantSyncJob(triggerType + "-parallel"));

        festivalThread.start();
        restaurantThread.start();

        try {
            festivalThread.join();
            restaurantThread.join();
            log.info("[Batch] {} 트리거: 모든 배치 작업 병렬 실행 완료", triggerType);
        } catch (InterruptedException e) {
            log.error("[Batch] {} 트리거: 병렬 배치 실행 중 인터럽트 발생", triggerType, e);
            Thread.currentThread().interrupt();
        }
    }
}