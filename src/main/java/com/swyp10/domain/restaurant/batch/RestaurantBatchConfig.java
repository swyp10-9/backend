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

    @Value("${tourapi.batch.restaurant.skip-if-data-exists:true}")
    private boolean skipIfDataExists;

    @Value("${tourapi.batch.restaurant.min-data-threshold:1}")
    private int minDataThreshold;

    @Value("${tourapi.service-key}")
    private String serviceKey;

    @Value("${tourapi.batch.restaurant.content-type-id:39}")
    private String contentTypeId;

    @Value("${tourapi.batch.restaurant.page-size:100}")
    private int pageSize;

    @Value("${tourapi.batch.restaurant.max-total-items:100}")
    private int maxTotalItems;

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
            .<Object, RestaurantProcessedData>chunk(10, transactionManager)  // 10개씩 처리
            .reader(restaurantItemReader())
            .processor(restaurantItemProcessor())
            .writer(restaurantItemWriter())
            .build();
    }

    // 기존 Tasklet 방식 (백업용)
    @Bean
    public Tasklet restaurantSyncTasklet() {
        RestaurantBatchProcessor processor = new RestaurantBatchProcessor(
            tourApiClient, restaurantService, objectMapper, serviceKey
        );

        return (contribution, chunkContext) -> {
            // 데이터 존재 여부 확인
            if (skipIfDataExists && shouldSkipBatch()) {
                log.info("[Restaurant Batch] Skipping batch - sufficient data already exists (count >= {})", minDataThreshold);
                return RepeatStatus.FINISHED;
            }

            log.info("Restaurant sync started - contentTypeId: {}, maxItems: {}", contentTypeId, maxTotalItems);

            BatchResult result = processor.processRestaurantBatch(contentTypeId, pageSize, maxTotalItems);

            log.info("Restaurant sync completed - Success: {}, Skipped: {}, Errors: {}",
                result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());

            return RepeatStatus.FINISHED;
        };
    }

    /**
     * 배치를 건너뛸지 여부 판단
     */
    private boolean shouldSkipBatch() {
        try {
            long existingDataCount = restaurantService.getTotalRestaurantCount();
            log.info("[Restaurant Batch] Current data count: {}, threshold: {}", existingDataCount, minDataThreshold);
            return existingDataCount >= minDataThreshold;
        } catch (Exception e) {
            log.warn("[Restaurant Batch] Failed to check existing data count, proceeding with batch: {}", e.getMessage());
            return false;
        }
    }

    // 메모리 최적화된 새로운 방식
    @Bean
    public RestaurantItemReader restaurantItemReader() {
        return new RestaurantItemReader(tourApiClient, serviceKey, contentTypeId, pageSize, maxTotalItems);
    }

    @Bean
    public RestaurantItemProcessor restaurantItemProcessor() {
        return new RestaurantItemProcessor(tourApiClient, serviceKey, contentTypeId, objectMapper);
    }

    @Bean
    public RestaurantItemWriter restaurantItemWriter() {
        return new RestaurantItemWriter(restaurantService);
    }
}
