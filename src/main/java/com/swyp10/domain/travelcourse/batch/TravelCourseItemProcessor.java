package com.swyp10.domain.travelcourse.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@RequiredArgsConstructor
public class TravelCourseItemProcessor implements ItemProcessor<Object, TravelCourseProcessedData> {

    private final TourApiClient tourApiClient;
    private final String serviceKey;
    private final ObjectMapper objectMapper;

    private final TravelCourseApiCaller apiCaller;

    public TravelCourseItemProcessor(TourApiClient tourApiClient, String serviceKey, ObjectMapper objectMapper) {
        this.tourApiClient = tourApiClient;
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
        this.apiCaller = new TravelCourseApiCaller(tourApiClient, serviceKey);
    }

    @Override
    public TravelCourseProcessedData process(Object item) throws Exception {
        if (!(item instanceof SearchTravelCourseDto)) {
            return null;
        }

        SearchTravelCourseDto travelCourse = (SearchTravelCourseDto) item;
        
        try {
            String contentId = travelCourse.getContentid();
            
            var detailInfo = apiCaller.fetchDetailInfo(contentId);

            return new TravelCourseProcessedData(travelCourse, detailInfo);

        } catch (Exception e) {
            log.warn("Failed to process travel course {}: {}", travelCourse.getContentid(), e.getMessage());
            return null;
        }
    }
}
