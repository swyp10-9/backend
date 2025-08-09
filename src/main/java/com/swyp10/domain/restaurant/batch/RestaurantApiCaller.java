package com.swyp10.domain.restaurant.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.restaurant.dto.tourapi.DetailInfo2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailIntro2RestaurantDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.UnknownContentTypeException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class RestaurantApiCaller {

    private final TourApiClient tourApiClient;
    private final String serviceKey;
    private final RestaurantBatchUtils batchUtils;

    public RestaurantApiCaller(TourApiClient tourApiClient, String serviceKey) {
        this.tourApiClient = tourApiClient;
        this.serviceKey = serviceKey;
        this.batchUtils = new RestaurantBatchUtils(new ObjectMapper());
    }

    /**
     * 안전한 API 호출 - 예외 처리 포함
     */
    public <T> T safeCall(Supplier<T> apiCall, T fallbackValue) {
        try {
            return apiCall.get();
        } catch (UnknownContentTypeException e) {
            log.warn("XML response received, using fallback");
            return fallbackValue;
        } catch (FeignException e) {
            log.warn("API error: {}, using fallback", e.getMessage());
            return fallbackValue;
        } catch (Exception e) {
            log.warn("Unexpected error: {}, using fallback", e.getMessage());
            return fallbackValue;
        }
    }

    /**
     * 안전한 API 호출 - 기본 null 반환
     */
    public <T> T safeCall(Supplier<T> apiCall) {
        return safeCall(apiCall, null);
    }

    public DetailIntro2RestaurantDto fetchDetailIntro(String contentId, String contentTypeId) {
        Map<String, Object> response = safeCall(() ->
            tourApiClient.detailIntro2(serviceKey, "ETC", "swyp10", "json", contentId, contentTypeId));

        return response != null ? batchUtils.parseDetailIntroRestaurantDto(response) : new DetailIntro2RestaurantDto();
    }

    public List<DetailInfo2RestaurantDto> fetchDetailInfo(String contentId, String contentTypeId) {
        Map<String, Object> response = safeCall(() ->
            tourApiClient.detailInfo2(serviceKey, "ETC", "swyp10", "json", contentId, contentTypeId));

        return response != null ? batchUtils.parseDetailInfoMenuList(response) : Collections.emptyList();
    }
}