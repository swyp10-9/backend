package com.swyp10.batch.controller;

import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job festivalSyncJob;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/run-festival-sync")
    public ResponseEntity<Map<String, Object>> runFestivalSyncJob() {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        JobExecution jobExecution = null;
        try {
            jobExecution = jobLauncher.run(festivalSyncJob, params);
        } catch (Exception e) {
            log.error("Failed to run festival sync job", e);
            throw new ApplicationException(ErrorCode.EXTERNAL_API_ERROR);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("jobExecutionId", jobExecution.getId());
        response.put("status", jobExecution.getStatus().toString());
        response.put("startTime", jobExecution.getStartTime());

        return ResponseEntity.ok(response);
    }
}
