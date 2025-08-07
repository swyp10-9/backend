package com.swyp10.domain.festival.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "tourApiClient",
    url = "http://apis.data.go.kr/B551011/KorService2",
    configuration = TourApiFeignConfig.class,
    fallback = TourApiClientFallback.class
)
public interface TourApiClient {

    @GetMapping("/searchFestival2")
    Map<String, Object> searchFestival2(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOS,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("numOfRows") int numOfRows,
        @RequestParam("pageNo") int pageNo,
        @RequestParam("eventStartDate") String eventStartDate,
        @RequestParam("eventEndDate") String eventEndDate
    );

    @GetMapping("/detailCommon2")
    Map<String, Object> detailCommon2(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOS,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("contentId") String contentId
    );

    @GetMapping("/detailIntro2")
    Map<String, Object> detailIntro2(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOS,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("contentId") String contentId,
        @RequestParam("contentTypeId") String contentTypeId
    );

    @GetMapping("/detailImage2")
    Map<String, Object> detailImage2(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOS,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("contentId") String contentId,
        @RequestParam("imageYN") String imageYN
    );

    @GetMapping("/areaCode2")
    Map<String, Object> areaCode2(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOS,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type
    );

    @GetMapping("/ldongCode2")
    Map<String, Object> ldongCode2(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOS,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type
    );

    @GetMapping("/areaBasedList2")
    Map<String, Object> areaBasedList2(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOS,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("contentTypeId") String contentTypeId,
        @RequestParam("numOfRows") int numOfRows,
        @RequestParam("pageNo") int pageNo
    );

    @GetMapping("/areaBasedList2")
    Map<String, Object> areaBasedList2(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOS,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("contentTypeId") String contentTypeId,
        @RequestParam("areaCode") String areaCode,
        @RequestParam("sigunguCode") String sigunguCode,
        @RequestParam("numOfRows") int numOfRows,
        @RequestParam("pageNo") int pageNo
    );

    @GetMapping("/detailInfo2")
    Map<String, Object> detailInfo2(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("MobileOS") String mobileOS,
        @RequestParam("MobileApp") String mobileApp,
        @RequestParam("_type") String type,
        @RequestParam("contentId") String contentId,
        @RequestParam("contentTypeId") String contentTypeId
    );
}