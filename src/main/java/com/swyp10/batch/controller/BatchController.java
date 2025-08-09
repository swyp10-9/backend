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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job festivalSyncJob;
    private final Job restaurantSyncJob;
    private final Job travelCourseSyncJob;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/run-festival-sync")
    public ResponseEntity<Map<String, Object>> runFestivalSyncJob() {
        log.info("Festival sync job requested by admin");

        JobParameters params = createJobParameters();
        JobExecution jobExecution = executeJob(festivalSyncJob, params, "festival sync");

        return ResponseEntity.ok(createJobResponse(jobExecution));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/run-restaurant-sync")
    public ResponseEntity<Map<String, Object>> runRestaurantSyncJob() {
        log.info("Restaurant sync job requested by admin");

        JobParameters params = createJobParameters();
        JobExecution jobExecution = executeJob(restaurantSyncJob, params, "restaurant sync");

        return ResponseEntity.ok(createJobResponse(jobExecution));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/run-travel-course-sync")
    public ResponseEntity<Map<String, Object>> runTravelCourseSyncJob() {
        log.info("TravelCourse sync job requested by admin");

        JobParameters params = createJobParameters();
        JobExecution jobExecution = executeJob(travelCourseSyncJob, params, "travel course sync");

        return ResponseEntity.ok(createJobResponse(jobExecution));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/run-all-sync")
    public ResponseEntity<Map<String, Object>> runAllSyncJobs() {
        log.info("All sync jobs requested by admin");

        Map<String, Object> response = new HashMap<>();

        try {
            // Festival 배치 실행
            JobParameters festivalParams = createJobParameters("festival");
            JobExecution festivalExecution = executeJob(festivalSyncJob, festivalParams, "festival sync");

            // Restaurant 배치 실행
            JobParameters restaurantParams = createJobParameters("restaurant");
            JobExecution restaurantExecution = executeJob(restaurantSyncJob, restaurantParams, "restaurant sync");

            // TravelCourse 배치 실행
            JobParameters courseParams = createJobParameters("travel-course");
            JobExecution courseExecution = executeJob(travelCourseSyncJob, courseParams, "travel course sync");

            response.put("festivalJob", createJobResponse(festivalExecution));
            response.put("restaurantJob", createJobResponse(restaurantExecution));
            response.put("travelCourseJob", createJobResponse(courseExecution));
            response.put("message", "All sync jobs started successfully");

        } catch (Exception e) {
            log.error("Failed to run all sync jobs", e);
            response.put("error", "Failed to start some jobs: " + e.getMessage());
            throw new ApplicationException(ErrorCode.EXTERNAL_API_ERROR);
        }

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{jobExecutionId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long jobExecutionId) {
        // JobExecution 조회 로직은 JobExplorer나 JobRepository를 통해 구현 필요
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Job status check - implement with JobExplorer");
        response.put("jobExecutionId", jobExecutionId);

        return ResponseEntity.ok(response);
    }

    /**
     * 공통 Job 실행 메서드
     */
    private JobExecution executeJob(Job job, JobParameters params, String jobDescription) {
        try {
            log.info("Starting {} job with parameters: {}", jobDescription, params);
            JobExecution execution = jobLauncher.run(job, params);
            log.info("{} job started successfully. Execution ID: {}", jobDescription, execution.getId());
            return execution;

        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("{} job is already running", jobDescription);
            throw new ApplicationException(ErrorCode.EXTERNAL_API_ERROR,
                jobDescription + " job is already running");

        } catch (JobRestartException e) {
            log.error("Failed to restart {} job", jobDescription, e);
            throw new ApplicationException(ErrorCode.EXTERNAL_API_ERROR,
                "Failed to restart " + jobDescription + " job");

        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("{} job instance already completed", jobDescription);
            throw new ApplicationException(ErrorCode.EXTERNAL_API_ERROR,
                jobDescription + " job instance already completed");

        } catch (JobParametersInvalidException e) {
            log.error("Invalid job parameters for {} job", jobDescription, e);
            throw new ApplicationException(ErrorCode.EXTERNAL_API_ERROR,
                "Invalid parameters for " + jobDescription + " job");

        } catch (Exception e) {
            log.error("Unexpected error running {} job", jobDescription, e);
            throw new ApplicationException(ErrorCode.EXTERNAL_API_ERROR,
                "Failed to run " + jobDescription + " job: " + e.getMessage());
        }
    }

    /**
     * JobParameters 생성 (기본)
     */
    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
    }

    /**
     * JobParameters 생성 (구분자 포함)
     */
    private JobParameters createJobParameters(String jobType) {
        return new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("jobType", jobType)
            .toJobParameters();
    }

    /**
     * Job 응답 생성
     */
    private Map<String, Object> createJobResponse(JobExecution jobExecution) {
        Map<String, Object> response = new HashMap<>();
        response.put("jobExecutionId", jobExecution.getId());
        response.put("jobName", jobExecution.getJobInstance().getJobName());
        response.put("status", jobExecution.getStatus().toString());
        response.put("startTime", jobExecution.getStartTime());
        response.put("endTime", jobExecution.getEndTime());

        // 배치 파라미터 정보
        Map<String, Object> parameters = new HashMap<>();
        jobExecution.getJobParameters().getParameters().forEach((key, value) -> {
            parameters.put(key, value.getValue());
        });
        response.put("parameters", parameters);

        return response;
    }
}