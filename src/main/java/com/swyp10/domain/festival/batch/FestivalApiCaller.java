package com.swyp10.domain.festival.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.festival.dto.tourapi.DetailCommon2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailImage2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailIntro2Dto;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.UnknownContentTypeException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class FestivalApiCaller {

    private final TourApiClient tourApiClient;
    private final String serviceKey;
    private final FestivalBatchUtils batchUtils;

    public FestivalApiCaller(TourApiClient tourApiClient, String serviceKey) {
        this.tourApiClient = tourApiClient;
        this.serviceKey = serviceKey;
        this.batchUtils = new FestivalBatchUtils(new ObjectMapper());
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

    public DetailCommon2Dto fetchDetailCommon(String contentId) {
        Map<String, Object> response = safeCall(() ->
            tourApiClient.detailCommon2(serviceKey, "ETC", "swyp10", "json", contentId));

        return response != null ? batchUtils.parseDetailCommon2Dto(response) : new DetailCommon2Dto();
    }

    public DetailIntro2Dto fetchDetailIntro(String contentId) {
        Map<String, Object> response = safeCall(() ->
            tourApiClient.detailIntro2(serviceKey, "ETC", "swyp10", "json", contentId, "15"));

        return response != null ? batchUtils.parseDetailIntro2Dto(response) : new DetailIntro2Dto();
    }

    public List<DetailImage2Dto> fetchDetailImages(String contentId) {
        Map<String, Object> response = safeCall(() ->
            tourApiClient.detailImage2(serviceKey, "ETC", "swyp10", "json", contentId, "Y"));

        return response != null ? batchUtils.parseDetailImageList2Dto(response) : Collections.emptyList();
    }
}