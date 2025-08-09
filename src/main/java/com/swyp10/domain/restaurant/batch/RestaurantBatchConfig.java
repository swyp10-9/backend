package com.swyp10.domain.restaurant.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.batch.BatchResult;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class RestaurantBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RestaurantService restaurantService;
    @Qualifier("com.swyp10.domain.festival.client.TourApiClient")
    private final TourApiClient tourApiClient;
    private final ObjectMapper objectMapper;

    @Value("${tourapi.service-key}")
    private String serviceKey;

    @Value("${tourapi.batch.restaurant.content-type-id:39}")
    private String contentTypeId;

    @Value("${tourapi.batch.restaurant.page-size:100}")
    private int pageSize;

    @Bean
    public Job restaurantSyncJob(Step restaurantSyncStep) {
        return new JobBuilder("restaurantSyncJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(restaurantSyncStep)
            .build();
    }

    @Bean
    public Step restaurantSyncStep() {
        return new StepBuilder("restaurantSyncStep", jobRepository)
            .tasklet(restaurantSyncTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet restaurantSyncTasklet() {
        RestaurantBatchProcessor processor = new RestaurantBatchProcessor(
            tourApiClient, restaurantService, objectMapper, serviceKey
        );

        return (contribution, chunkContext) -> {
            log.info("Restaurant sync started - contentTypeId: {}", contentTypeId);

            BatchResult result = processor.processRestaurantBatch(contentTypeId, pageSize);

            log.info("Restaurant sync completed - Success: {}, Skipped: {}, Errors: {}",
                result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());

            return RepeatStatus.FINISHED;
        };
    }
}