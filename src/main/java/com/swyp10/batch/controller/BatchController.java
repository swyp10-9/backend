package com.swyp10.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job festivalSyncJob;

    @PostMapping("/run-festival-sync")
    public String runFestivalSyncJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(festivalSyncJob, params);
        return "Batch job status: " + jobExecution.getStatus();
    }
}
