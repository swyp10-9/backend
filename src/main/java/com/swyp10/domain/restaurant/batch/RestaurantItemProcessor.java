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
    private final String contentTypeId;  // 추가
    private final ObjectMapper objectMapper;

    private final RestaurantApiCaller apiCaller;

    public RestaurantItemProcessor(TourApiClient tourApiClient, String serviceKey, String contentTypeId, ObjectMapper objectMapper) {
        this.tourApiClient = tourApiClient;
        this.serviceKey = serviceKey;
        this.contentTypeId = contentTypeId;  // 추가
        this.objectMapper = objectMapper;
        this.apiCaller = new RestaurantApiCaller(tourApiClient, serviceKey, contentTypeId);  // contentTypeId 전달
    }

    @Override
    public RestaurantProcessedData process(Object item) throws Exception {
        if (!(item instanceof AreaBasedList2RestaurantDto)) {
            return null;
        }

        AreaBasedList2RestaurantDto restaurant = (AreaBasedList2RestaurantDto) item;
        
        try {
            String contentId = restaurant.getContentid();
            
            // 상세 정보 가져오기 (메모리에 최소한만 유지)
            var detailInfo = apiCaller.fetchDetailInfo(contentId);
            var detailIntro = apiCaller.fetchDetailIntro(contentId);

            return new RestaurantProcessedData(restaurant, detailInfo, detailIntro);

        } catch (Exception e) {
            log.warn("Failed to process restaurant {}: {}", restaurant.getContentid(), e.getMessage());
            return null;
        }
    }
}
