package com.swyp10.domain.travelcourse.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.UnknownContentTypeException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class TravelCourseApiCaller {

    private final TourApiClient tourApiClient;
    private final String serviceKey;
    private final String contentTypeId;  // 추가
    private final TravelCourseBatchUtils batchUtils;

    public TravelCourseApiCaller(TourApiClient tourApiClient, String serviceKey) {
        this.tourApiClient = tourApiClient;
        this.serviceKey = serviceKey;
        this.contentTypeId = "25";  // 여행코스 기본값
        this.batchUtils = new TravelCourseBatchUtils(new ObjectMapper());
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

    public List<DetailInfoCourseDto> fetchDetailInfo(String contentId) {
        Map<String, Object> response = safeCall(() ->
            tourApiClient.detailInfo2(serviceKey, "ETC", "swyp10", "json", contentId, contentTypeId));  // 변수 사용

        return response != null ? batchUtils.parseDetailInfoCourseList(response) : Collections.emptyList();
    }
}