package com.swyp10.domain.restaurant.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailInfo2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailIntro2RestaurantDto;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class RestaurantBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RestaurantService restaurantService;
    private final TourApiClient tourApiClient;
    private final ObjectMapper objectMapper;

    @Value("${tourapi.service-key}")
    private String SERVICE_KEY;

    @Bean
    public Job restaurantSyncJob(Step restaurantSyncStep) {
        return new JobBuilder("restaurantSyncJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(restaurantSyncStep)
            .build();
    }

    @Bean
    public Step restaurantSyncStep(Tasklet restaurantSyncTasklet) {
        return new StepBuilder("restaurantSyncStep", jobRepository)
            .tasklet(restaurantSyncTasklet, transactionManager)
            .build();
    }

    @Bean
    public Tasklet restaurantSyncTasklet() {
        return (contribution, chunkContext) -> {
            int page = 1;
            int pageSize = 10;
            int totalCount;

            RestaurantBatchUtils batchUtils = new RestaurantBatchUtils(objectMapper);

            do {
                Map<String, Object> response = tourApiClient.areaBasedList2(
                    SERVICE_KEY, "ETC", "swyp10", "json", "39", pageSize, page
                );

                Map<String, Object> body = batchUtils.getNestedMap(response, "response", "body");
                totalCount = Integer.parseInt(String.valueOf(body.get("totalCount")));

                Map<String, Object> items = (Map<String, Object>) body.get("items");
                if (items == null || items.get("item") == null) break;

                List<Map<String, Object>> restaurantList;
                Object itemObj = items.get("item");
                if (itemObj instanceof List<?>) {
                    restaurantList = (List<Map<String, Object>>) itemObj;
                } else {
                    restaurantList = List.of((Map<String, Object>) itemObj);
                }

                for (Map<String, Object> item : restaurantList) {
                    AreaBasedList2RestaurantDto restaurantDto = batchUtils.parseSearchRestaurantDto(item);

                    Map<String, Object> introResponse = tourApiClient.detailIntro2(
                        SERVICE_KEY, "ETC", "swyp10", "json",
                        restaurantDto.getContentId(), restaurantDto.getContentTypeId()
                    );
                    DetailIntro2RestaurantDto restaurantIntroDto = batchUtils.parseDetailIntroRestaurantDto(introResponse);

                    Map<String, Object> infoResponse = tourApiClient.detailInfo2(
                        SERVICE_KEY, "ETC", "swyp10", "json",
                        restaurantDto.getContentId(), restaurantDto.getContentTypeId()
                    );
                    List<DetailInfo2RestaurantDto> restaurantInfoDto = batchUtils.parseDetailInfoMenuList(infoResponse);

                    restaurantService.saveOrUpdateRestaurant(restaurantDto, restaurantIntroDto, restaurantInfoDto);
                }
                page++;
            } while ((page - 1) * pageSize < totalCount);

            return RepeatStatus.FINISHED;
        };
    }
}
