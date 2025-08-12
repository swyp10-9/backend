package com.swyp10.domain.travelcourse.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.batch.BatchResult;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import com.swyp10.domain.travelcourse.service.TravelCourseService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class TravelCourseBatchProcessor {

    private final TourApiClient tourApiClient;
    private final TravelCourseService travelCourseService;
    private final ObjectMapper objectMapper;
    private final String serviceKey;

    private final TravelCourseApiCaller apiCaller;
    private final TravelCourseBatchUtils batchUtils;

    public TravelCourseBatchProcessor(TourApiClient tourApiClient, TravelCourseService travelCourseService,
                                      ObjectMapper objectMapper, String serviceKey) {
        this.tourApiClient = tourApiClient;
        this.travelCourseService = travelCourseService;
        this.objectMapper = objectMapper;
        this.serviceKey = serviceKey;
        this.apiCaller = new TravelCourseApiCaller(tourApiClient, serviceKey);
        this.batchUtils = new TravelCourseBatchUtils(objectMapper);
    }

    public BatchResult processTravelCourseBatch(String contentTypeId, int pageSize) {
        return processTravelCourseBatch(contentTypeId, pageSize, Integer.MAX_VALUE);
    }

    public BatchResult processTravelCourseBatch(String contentTypeId, int pageSize, int maxItems) {
        BatchResult result = new BatchResult();

        try {
            int page = 1;
            int totalCount = 0;
            int processedItems = 0;

            log.info("TravelCourse batch started - maxItems: {}", maxItems == Integer.MAX_VALUE ? "unlimited" : maxItems);

            do {
                List<SearchTravelCourseDto> travelCourses = fetchTravelCoursePage(contentTypeId, pageSize, page);

                if (travelCourses.isEmpty()) {
                    break;
                }

                if (page == 1) {
                    totalCount = getTotalCount(contentTypeId, pageSize);
                    int actualTotal = Math.min(totalCount, maxItems);
                    log.info("Found {} total travel courses, processing {} items", totalCount, actualTotal);
                }

                // 최대 개수 제한 체크
                if (processedItems + travelCourses.size() > maxItems) {
                    // 남은 개수만큼만 처리
                    int remainingItems = maxItems - processedItems;
                    travelCourses = travelCourses.subList(0, Math.max(0, remainingItems));
                    log.info("Limiting to {} remaining items to reach max limit", remainingItems);
                }

                processTravelCoursePage(travelCourses, result);
                processedItems += travelCourses.size();

                // 최대 개수에 도달하면 중단
                if (processedItems >= maxItems) {
                    log.info("Reached maximum item limit: {}, stopping batch", maxItems);
                    break;
                }

                page++;
                Thread.sleep(100); // API 부하 방지

            } while ((page - 1) * pageSize < totalCount);

            log.info("TravelCourse batch completed - processed: {}/{} items", processedItems, maxItems == Integer.MAX_VALUE ? "unlimited" : maxItems);

        } catch (Exception e) {
            log.error("TravelCourse batch processing failed", e);
            throw new RuntimeException("TravelCourse batch failed", e);
        }

        return result;
    }

    private List<SearchTravelCourseDto> fetchTravelCoursePage(String contentTypeId, int pageSize, int page) {
        try {
            Map<String, Object> response = apiCaller.safeCall(() ->
                tourApiClient.areaBasedList2(serviceKey, "ETC", "swyp10", "json",
                    contentTypeId, pageSize, page));

            return batchUtils.parseTravelCourseList(response);

        } catch (Exception e) {
            log.warn("Failed to fetch travel course page {}: {}", page, e.getMessage());
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

    private void processTravelCoursePage(List<SearchTravelCourseDto> travelCourses, BatchResult result) {
        for (SearchTravelCourseDto travelCourse : travelCourses) {
            try {
                processSingleTravelCourse(travelCourse);
                result.incrementSuccess();

                if (result.getTotalProcessed() % 10 == 0) {
                    log.info("Progress: {}", result);
                }

            } catch (Exception e) {
                log.warn("Failed to process travel course {}: {}", travelCourse.getContentid(), e.getMessage());
                result.incrementError();
            }
        }
    }

    private void processSingleTravelCourse(SearchTravelCourseDto searchDto) {
        String contentId = searchDto.getContentid();

        var detailInfoDtos = apiCaller.fetchDetailInfo(contentId);

        travelCourseService.saveOrUpdateTravelCourse(searchDto, detailInfoDtos);
    }
}