package com.swyp10.domain.festival.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.festival.dto.tourapi.DetailCommon2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailImage2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailIntro2Dto;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private String SERVICE_KEY;

    @Value("${tourapi.batch.festival.event-start-date}")
    private String eventStartDate;

    @Value("${tourapi.batch.festival.event-end-date}")
    private String eventEndDate;

    @Value("${tourapi.batch.page-size}")
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
    public Step festivalSyncStep(Tasklet festivalSyncTasklet) {
        return new StepBuilder("festivalSyncStep", jobRepository)
            .tasklet(festivalSyncTasklet, transactionManager)
            .build();
    }

    @Bean
    public Tasklet festivalSyncTasklet() {
        return (contribution, chunkContext) -> {
            int page = 1;
            int totalCount;
            do {
                Map<String, Object> response = tourApiClient.searchFestival2(
                    SERVICE_KEY, "ETC", "swyp10", "json", pageSize, page, eventStartDate, eventEndDate
                );

                Map<String, Object> body = FestivalBatchUtils.getNestedMap(response, "response", "body");
                totalCount = Integer.parseInt(String.valueOf(body.get("totalCount")));

                Map<String, Object> items = (Map<String, Object>) body.get("items");
                if (items == null) break;

                List<Map<String, Object>> festivalList;
                Object itemObj = items.get("item");
                if (itemObj instanceof List<?>) {
                    festivalList = (List<Map<String, Object>>) itemObj;
                } else if (itemObj instanceof Map<?,?>) {
                    festivalList = List.of((Map<String, Object>) itemObj);
                } else {
                    break;
                }

                FestivalBatchUtils festivalBatchUtils = new FestivalBatchUtils(objectMapper);
                for (Map<String, Object> item : festivalList) {
                    SearchFestival2Dto searchDto = festivalBatchUtils.parseSearchFestival2Dto(item);

                    Map<String, Object> commonResponse = tourApiClient.detailCommon2(
                        SERVICE_KEY, "ETC", "swyp10", "json", searchDto.getContentid()
                    );
                    DetailCommon2Dto commonDto = festivalBatchUtils.parseDetailCommon2Dto(commonResponse);

                    Map<String, Object> introResponse = tourApiClient.detailIntro2(
                        SERVICE_KEY, "ETC", "swyp10", "json", searchDto.getContentid(), "15"
                    );
                    DetailIntro2Dto introDto = festivalBatchUtils.parseDetailIntro2Dto(introResponse);

                    Map<String, Object> imageResponse = tourApiClient.detailImage2(
                        SERVICE_KEY, "ETC", "swyp10", "json", searchDto.getContentid(), "Y"
                    );
                    List<DetailImage2Dto> images = festivalBatchUtils.parseDetailImageList2Dto(imageResponse);

                    festivalService.saveOrUpdateFestival(searchDto, commonDto, introDto, images);
                }
                page++;
            } while ((page - 1) * pageSize < totalCount);

            return RepeatStatus.FINISHED;
        };
    }
}
