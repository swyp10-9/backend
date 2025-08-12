package com.swyp10.domain.travelcourse.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.batch.BatchResult;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.travelcourse.service.TravelCourseService;
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
public class TravelCourseBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TravelCourseService travelCourseService;
    @Qualifier("com.swyp10.domain.festival.client.TourApiClient")
    private final TourApiClient tourApiClient;
    private final ObjectMapper objectMapper;

    @Value("${tourapi.batch.travel-course.skip-if-data-exists:true}")
    private boolean skipIfDataExists;

    @Value("${tourapi.batch.travel-course.min-data-threshold:1}")
    private int minDataThreshold;

    @Value("${tourapi.service-key}")
    private String serviceKey;

    @Value("${tourapi.batch.travel-course.content-type-id:25}")
    private String contentTypeId;

    @Value("${tourapi.batch.travel-course.page-size:100}")
    private int pageSize;

    @Value("${tourapi.batch.travel-course.max-total-items:100}")
    private int maxTotalItems;

    @Bean
    public Job travelCourseSyncJob(Step travelCourseSyncStep) {
        return new JobBuilder("travelCourseSyncJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(travelCourseSyncStep)
            .build();
    }

    @Bean
    public Step travelCourseSyncStep() {
        return new StepBuilder("travelCourseSyncStep", jobRepository)
            .<Object, TravelCourseProcessedData>chunk(10, transactionManager)  // 10개씩 처리
            .reader(travelCourseItemReader())
            .processor(travelCourseItemProcessor())
            .writer(travelCourseItemWriter())
            .build();
    }

    // 기존 Tasklet 방식 (백업용)
    @Bean
    public Tasklet travelCourseSyncTasklet() {
        TravelCourseBatchProcessor processor = new TravelCourseBatchProcessor(
            tourApiClient, travelCourseService, objectMapper, serviceKey
        );

        return (contribution, chunkContext) -> {
            // 데이터 존재 여부 확인
            if (skipIfDataExists && shouldSkipBatch()) {
                log.info("[TravelCourse Batch] Skipping batch - sufficient data already exists (count >= {})", minDataThreshold);
                return RepeatStatus.FINISHED;
            }

            log.info("TravelCourse sync started - contentTypeId: {}, maxItems: {}", contentTypeId, maxTotalItems);

            BatchResult result = processor.processTravelCourseBatch(contentTypeId, pageSize, maxTotalItems);

            log.info("TravelCourse sync completed - Success: {}, Skipped: {}, Errors: {}",
                result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());

            return RepeatStatus.FINISHED;
        };
    }

    /**
     * 배치를 건너뛸지 여부 판단
     */
    private boolean shouldSkipBatch() {
        try {
            long existingDataCount = travelCourseService.getTotalTravelCourseCount();
            log.info("[TravelCourse Batch] Current data count: {}, threshold: {}", existingDataCount, minDataThreshold);
            return existingDataCount >= minDataThreshold;
        } catch (Exception e) {
            log.warn("[TravelCourse Batch] Failed to check existing data count, proceeding with batch: {}", e.getMessage());
            return false;
        }
    }

    // 메모리 최적화된 새로운 방식
    @Bean
    public TravelCourseItemReader travelCourseItemReader() {
        return new TravelCourseItemReader(tourApiClient, serviceKey, contentTypeId, pageSize, maxTotalItems);
    }

    @Bean
    public TravelCourseItemProcessor travelCourseItemProcessor() {
        return new TravelCourseItemProcessor(tourApiClient, serviceKey, contentTypeId, objectMapper);
    }

    @Bean
    public TravelCourseItemWriter travelCourseItemWriter() {
        return new TravelCourseItemWriter(travelCourseService);
    }
}
