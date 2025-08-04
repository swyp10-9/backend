package com.swyp10.domain.travelcourse.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import com.swyp10.domain.travelcourse.service.TravelCourseService;
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
public class TravelCourseBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TravelCourseService travelCourseService;
    private final TourApiClient tourApiClient;
    private final ObjectMapper objectMapper;

    private static final String SERVICE_KEY = "발급받은_API_키";

    @Bean
    public Job travelCourseSyncJob(Step travelCourseSyncStep) {
        return new JobBuilder("travelCourseSyncJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .flow(travelCourseSyncStep)
            .end()
            .build();
    }

    @Bean
    public Step travelCourseSyncStep(Tasklet travelCourseSyncTasklet) {
        return new StepBuilder("travelCourseSyncStep", jobRepository)
            .tasklet(travelCourseSyncTasklet, transactionManager)
            .build();
    }

    @Bean
    public Tasklet travelCourseSyncTasklet() {
        return (contribution, chunkContext) -> {
            int page = 1;
            int pageSize = 10;
            int totalCount;

            TravelCourseBatchUtils batchUtils = new TravelCourseBatchUtils(objectMapper);

            do {
                Map<String, Object> response = tourApiClient.areaBasedList2(
                    SERVICE_KEY, "ETC", "swyp10", "json", "25", pageSize, page
                );

                Map<String, Object> body = batchUtils.getNestedMap(response, "response", "body");
                totalCount = Integer.parseInt(String.valueOf(body.get("totalCount")));

                Map<String, Object> items = (Map<String, Object>) body.get("items");
                if (items == null || items.get("item") == null) break;

                List<Map<String, Object>> courseList;
                Object itemObj = items.get("item");
                if (itemObj instanceof List<?>) {
                    courseList = (List<Map<String, Object>>) itemObj;
                } else {
                    courseList = List.of((Map<String, Object>) itemObj);
                }

                for (Map<String, Object> item : courseList) {
                    SearchTravelCourseDto searchDto = batchUtils.parseSearchTravelCourseDto(item);

                    Map<String, Object> detailInfoResponse = tourApiClient.detailInfo2(
                        SERVICE_KEY, "ETC", "swyp10", "json",
                        searchDto.getContentid(), searchDto.getContenttypeid()
                    );
                    List<DetailInfoCourseDto> detailInfoDtos = batchUtils.parseDetailInfoCourseList(detailInfoResponse);

                    travelCourseService.saveOrUpdateTravelCourse(searchDto, detailInfoDtos);
                }
                page++;
            } while ((page - 1) * pageSize < totalCount);

            return RepeatStatus.FINISHED;
        };
    }
}
