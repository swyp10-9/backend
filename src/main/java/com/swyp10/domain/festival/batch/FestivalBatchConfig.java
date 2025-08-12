package com.swyp10.domain.festival.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import com.swyp10.domain.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@Profile("!test")
@RequiredArgsConstructor
@EnableBatchProcessing
public class FestivalBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FestivalService festivalService;
    private final TourApiClient tourApiClient;
    private final ObjectMapper objectMapper;

    @Value("${tourapi.batch.festival.skip-if-data-exists:true}")
    private boolean skipIfDataExists;

    @Value("${tourapi.batch.festival.min-data-threshold:1}")
    private int minDataThreshold;

    @Value("${tourapi.service-key}")
    private String serviceKey;

    @Value("${tourapi.batch.festival.event-start-date}")
    private String eventStartDate;

    @Value("${tourapi.batch.festival.event-end-date}")
    private String eventEndDate;

    @Value("${tourapi.batch.festival.incremental-mode:true}")
    private boolean incrementalMode;

    @Value("${tourapi.batch.festival.incremental-days:30}")
    private int incrementalDays;

    @Value("${tourapi.batch.page-size:100}")
    private int pageSize;

    @Bean
    public Job festivalSyncJob(Step festivalSyncStep) {
        return new JobBuilder("festivalSyncJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .flow(festivalSyncStep)
            .end()
            .build();
    }

    @Bean
    public Step festivalSyncStep() {
        return new StepBuilder("festivalSyncStep", jobRepository)
            .<SearchFestival2Dto, FestivalProcessedData>chunk(10, transactionManager)  // 10개씩 처리
            .reader(festivalItemReader())
            .processor(festivalItemProcessor())
            .writer(festivalItemWriter())
            .build();
    }

    @Bean
    public Tasklet festivalSyncTasklet() {
        FestivalBatchProcessor processor = new FestivalBatchProcessor(
            tourApiClient, festivalService, objectMapper, serviceKey
        );

        return (contribution, chunkContext) -> {
            // Job Parameter에서 triggerType 확인
            String triggerType = chunkContext.getStepContext().getJobParameters().get("triggerType").toString();
            boolean isStartup = triggerType.equals("startup");
            
            // 데이터 존재 여부 확인 (startup만)
            if (isStartup && skipIfDataExists && shouldSkipBatch()) {
                log.info("[Festival Batch] Skipping startup batch - sufficient data already exists (count >= {})", minDataThreshold);
                return RepeatStatus.FINISHED;
            }

            // 무조건 증분 모드로 날짜 계산
            LocalDate now = LocalDate.now();
            LocalDate incrementalStart = now.minusDays(incrementalDays);
            String startDate = incrementalStart.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String endDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            log.info("[Festival Batch] Incremental mode: Processing festivals from {} to {} ({} days)", 
                startDate, endDate, incrementalDays);

            BatchResult result = processor.processFestivalBatch(startDate, endDate, pageSize);

            log.info("Festival sync completed - Success: {}, Skipped: {}, Errors: {}",
                result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());

            return RepeatStatus.FINISHED;
        };
    }

    /**
     * 배치를 건너뛸지 여부 판단
     */
    private boolean shouldSkipBatch() {
        try {
            long existingDataCount = festivalService.getTotalFestivalCount();
            log.info("[Festival Batch] Current data count: {}, threshold: {}", existingDataCount, minDataThreshold);
            return existingDataCount >= minDataThreshold;
        } catch (Exception e) {
            log.warn("[Festival Batch] Failed to check existing data count, proceeding with batch: {}", e.getMessage());
            return false;
        }
    }

    // 메모리 최적화된 새로운 방식
    @Bean
    public FestivalItemReader festivalItemReader() {
        // 무조건 증분 모드로 날짜 계산
        LocalDate now = LocalDate.now();
        LocalDate incrementalStart = now.minusDays(incrementalDays);
        String startDate = incrementalStart.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String endDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        log.info("[Festival Batch] Incremental mode: Processing festivals from {} to {} ({} days)", 
            startDate, endDate, incrementalDays);
        
        return new FestivalItemReader(tourApiClient, serviceKey, startDate, endDate, pageSize);
    }

    @Bean
    public FestivalItemProcessor festivalItemProcessor() {
        return new FestivalItemProcessor(tourApiClient, serviceKey, objectMapper);
    }

    @Bean
    public FestivalItemWriter festivalItemWriter() {
        return new FestivalItemWriter(festivalService);
    }
}
