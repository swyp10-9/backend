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

    @Value("${tourapi.service-key}")
    private String serviceKey;

    @Value("${tourapi.batch.festival.event-start-date}")
    private String eventStartDate;

    @Value("${tourapi.batch.festival.event-end-date}")
    private String eventEndDate;

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
            log.info("Festival sync started: {} ~ {}", eventStartDate, eventEndDate);

            BatchResult result = processor.processFestivalBatch(eventStartDate, eventEndDate, pageSize);

            log.info("Festival sync completed - Success: {}, Skipped: {}, Errors: {}",
                result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());

            return RepeatStatus.FINISHED;
        };
    }

    // 메모리 최적화된 새로운 방식
    @Bean
    public FestivalItemReader festivalItemReader() {
        return new FestivalItemReader(tourApiClient, serviceKey, eventStartDate, eventEndDate, pageSize);
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