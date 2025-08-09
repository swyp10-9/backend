package com.swyp10.domain.restaurant.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.batch.BatchResult;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import com.swyp10.domain.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class RestaurantBatchProcessor {

    private final TourApiClient tourApiClient;
    private final RestaurantService restaurantService;
    private final ObjectMapper objectMapper;
    private final String serviceKey;

    private final RestaurantApiCaller apiCaller;
    private final RestaurantBatchUtils batchUtils;

    public RestaurantBatchProcessor(TourApiClient tourApiClient, RestaurantService restaurantService,
                                    ObjectMapper objectMapper, String serviceKey) {
        this.tourApiClient = tourApiClient;
        this.restaurantService = restaurantService;
        this.objectMapper = objectMapper;
        this.serviceKey = serviceKey;
        this.apiCaller = new RestaurantApiCaller(tourApiClient, serviceKey);
        this.batchUtils = new RestaurantBatchUtils(objectMapper);
    }

    public BatchResult processRestaurantBatch(String contentTypeId, int pageSize) {
        BatchResult result = new BatchResult();

        try {
            int page = 1;
            int totalCount = 0;

            do {
                List<AreaBasedList2RestaurantDto> restaurants = fetchRestaurantPage(contentTypeId, pageSize, page);

                if (restaurants.isEmpty()) {
                    break;
                }

                if (page == 1) {
                    totalCount = getTotalCount(contentTypeId, pageSize);
                    log.info("Found {} total restaurants to process", totalCount);
                }

                processRestaurantPage(restaurants, result);

                page++;
                Thread.sleep(100); // API 부하 방지

            } while ((page - 1) * pageSize < totalCount);

        } catch (Exception e) {
            log.error("Restaurant batch processing failed", e);
            throw new RuntimeException("Restaurant batch failed", e);
        }

        return result;
    }

    private List<AreaBasedList2RestaurantDto> fetchRestaurantPage(String contentTypeId, int pageSize, int page) {
        try {
            Map<String, Object> response = apiCaller.safeCall(() ->
                tourApiClient.areaBasedList2(serviceKey, "ETC", "swyp10", "json",
                    contentTypeId, pageSize, page));

            return batchUtils.parseRestaurantList(response);

        } catch (Exception e) {
            log.warn("Failed to fetch restaurant page {}: {}", page, e.getMessage());
            return List.of();
        }
    }

    private int getTotalCount(String contentTypeId, int pageSize) {
        try {
            Map<String, Object> response = tourApiClient.areaBasedList2(
                serviceKey, "ETC", "swyp10", "json", contentTypeId, 1, 1);

            return batchUtils.extractTotalCount(response);

        } catch (Exception e) {
            log.warn("Failed to get total count, using default", e);
            return 1000; // 기본값
        }
    }

    private void processRestaurantPage(List<AreaBasedList2RestaurantDto> restaurants, BatchResult result) {
        for (AreaBasedList2RestaurantDto restaurant : restaurants) {
            try {
                processSingleRestaurant(restaurant);
                result.incrementSuccess();

                if (result.getTotalProcessed() % 10 == 0) {
                    log.info("Progress: {}", result);
                }

            } catch (Exception e) {
                log.warn("Failed to process restaurant {}: {}", restaurant.getContentId(), e.getMessage());
                result.incrementError();
            }
        }
    }

    private void processSingleRestaurant(AreaBasedList2RestaurantDto restaurantDto) {
        String contentId = restaurantDto.getContentId();
        String contentTypeId = restaurantDto.getContentTypeId();

        var introDto = apiCaller.fetchDetailIntro(contentId, contentTypeId);
        var infoDto = apiCaller.fetchDetailInfo(contentId, contentTypeId);

        restaurantService.saveOrUpdateRestaurant(restaurantDto, introDto, infoDto);
    }
}