package com.swyp10.domain.restaurant.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@RequiredArgsConstructor
public class RestaurantItemProcessor implements ItemProcessor<Object, RestaurantProcessedData> {

    private final TourApiClient tourApiClient;
    private final String serviceKey;
    private final ObjectMapper objectMapper;

    private final RestaurantApiCaller apiCaller;

    public RestaurantItemProcessor(TourApiClient tourApiClient, String serviceKey, ObjectMapper objectMapper) {
        this.tourApiClient = tourApiClient;
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
        this.apiCaller = new RestaurantApiCaller(tourApiClient, serviceKey);
    }

    @Override
    public RestaurantProcessedData process(Object item) throws Exception {
        if (!(item instanceof AreaBasedList2RestaurantDto)) {
            return null;
        }

        AreaBasedList2RestaurantDto restaurant = (AreaBasedList2RestaurantDto) item;
        
        try {
            String contentId = restaurant.getContentId();
            
            // 상세 정보 가져오기 (메모리에 최소한만 유지)
            var detailIntro = apiCaller.fetchDetailIntro(contentId);
            var detailInfo = apiCaller.fetchDetailInfo(contentId);

            return new RestaurantProcessedData(restaurant, detailInfo, detailIntro);

        } catch (Exception e) {
            log.warn("Failed to process restaurant {}: {}", restaurant.getContentId(), e.getMessage());
            return null;
        }
    }
}
