package com.swyp10.domain.festival.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TourApiClientFallback implements TourApiClient {

    /**
     * TourAPI 응답 구조에 맞는 기본 fallback 응답 생성
     */
    private Map<String, Object> createFallbackResponse(String apiName, String... params) {
        log.warn("TourAPI fallback triggered for {} with params: {}", apiName, String.join(", ", params));

        Map<String, Object> fallbackResponse = new HashMap<>();

        // TourAPI 표준 응답 구조
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> header = new HashMap<>();
        Map<String, Object> body = new HashMap<>();

        // 헤더 정보
        header.put("resultCode", "9999"); // fallback 코드
        header.put("resultMsg", "Service temporarily unavailable (fallback)");

        // 바디 정보 (빈 리스트)
        body.put("items", Collections.emptyList());
        body.put("numOfRows", 0);
        body.put("pageNo", 1);
        body.put("totalCount", 0);

        response.put("header", header);
        response.put("body", body);
        fallbackResponse.put("response", response);

        return fallbackResponse;
    }

    @Override
    public Map<String, Object> searchFestival2(String serviceKey, String mobileOS, String mobileApp,
                                               String type, int numOfRows, int pageNo,
                                               String eventStartDate, String eventEndDate) {
        return createFallbackResponse("searchFestival2",
            "numOfRows=" + numOfRows, "pageNo=" + pageNo,
            "eventStartDate=" + eventStartDate, "eventEndDate=" + eventEndDate);
    }

    @Override
    public Map<String, Object> detailCommon2(String serviceKey, String mobileOS, String mobileApp,
                                             String type, String contentId) {
        return createFallbackResponse("detailCommon2", "contentId=" + contentId);
    }

    @Override
    public Map<String, Object> detailIntro2(String serviceKey, String mobileOS, String mobileApp,
                                            String type, String contentId, String contentTypeId) {
        return createFallbackResponse("detailIntro2",
            "contentId=" + contentId, "contentTypeId=" + contentTypeId);
    }

    @Override
    public Map<String, Object> detailImage2(String serviceKey, String mobileOS, String mobileApp,
                                            String type, String contentId, String imageYN) {
        return createFallbackResponse("detailImage2",
            "contentId=" + contentId, "imageYN=" + imageYN);
    }

    @Override
    public Map<String, Object> areaCode2(String serviceKey, String mobileOS, String mobileApp, String type) {
        return createFallbackResponse("areaCode2");
    }

    @Override
    public Map<String, Object> ldongCode2(String serviceKey, String mobileOS, String mobileApp, String type) {
        return createFallbackResponse("ldongCode2");
    }

    @Override
    public Map<String, Object> areaBasedList2(String serviceKey, String mobileOS, String mobileApp,
                                              String type, String contentTypeId, int numOfRows, int pageNo) {
        return createFallbackResponse("areaBasedList2",
            "contentTypeId=" + contentTypeId, "numOfRows=" + numOfRows, "pageNo=" + pageNo);
    }

    @Override
    public Map<String, Object> areaBasedList2(String serviceKey, String mobileOS, String mobileApp,
                                              String type, String contentTypeId, String areaCode,
                                              String sigunguCode, int numOfRows, int pageNo) {
        return createFallbackResponse("areaBasedList2",
            "contentTypeId=" + contentTypeId, "areaCode=" + areaCode,
            "sigunguCode=" + sigunguCode, "numOfRows=" + numOfRows, "pageNo=" + pageNo);
    }

    @Override
    public Map<String, Object> detailInfo2(String serviceKey, String mobileOS, String mobileApp,
                                           String type, String contentId, String contentTypeId) {
        return createFallbackResponse("detailInfo2",
            "contentId=" + contentId, "contentTypeId=" + contentTypeId);
    }
}