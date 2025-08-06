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

@Slf4j
@RequiredArgsConstructor
@Component
public class FestivalBatchAutoRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job festivalSyncJob;

    // 서버 시작시 1회 실행
    @Override
    public void run(ApplicationArguments args) throws Exception {
        runFestivalSyncJob("startup");
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
