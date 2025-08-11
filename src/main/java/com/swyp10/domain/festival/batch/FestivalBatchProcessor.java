package com.swyp10.domain.festival.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import com.swyp10.domain.festival.service.FestivalService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class FestivalBatchProcessor {

    private final TourApiClient tourApiClient;
    private final FestivalService festivalService;
    private final ObjectMapper objectMapper;
    private final String serviceKey;

    private final FestivalApiCaller apiCaller;
    private final FestivalBatchUtils batchUtils;

    public FestivalBatchProcessor(TourApiClient tourApiClient, FestivalService festivalService,
                                  ObjectMapper objectMapper, String serviceKey) {
        this.tourApiClient = tourApiClient;
        this.festivalService = festivalService;
        this.objectMapper = objectMapper;
        this.serviceKey = serviceKey;
        this.apiCaller = new FestivalApiCaller(tourApiClient, serviceKey);
        this.batchUtils = new FestivalBatchUtils(objectMapper);
    }

    public BatchResult processFestivalBatch(String startDate, String endDate, int pageSize) {
        BatchResult result = new BatchResult();

        try {
            int page = 1;
            int totalCount = 0;

            do {
                List<SearchFestival2Dto> festivals = fetchFestivalPage(startDate, endDate, pageSize, page);

                if (festivals.isEmpty()) {
                    break;
                }

                if (page == 1) {
                    totalCount = getTotalCount(startDate, endDate, pageSize);
                    log.info("Found {} total festivals to process", totalCount);
                }

                processFestivalPage(festivals, result);

                page++;
                Thread.sleep(100); // API 부하 방지

            } while ((page - 1) * pageSize < totalCount);

        } catch (Exception e) {
            log.error("Festival batch processing failed", e);
            throw new RuntimeException("Festival batch failed", e);
        }

        return result;
    }

    private List<SearchFestival2Dto> fetchFestivalPage(String startDate, String endDate,
                                                       int pageSize, int page) {
        try {
            Map<String, Object> response = apiCaller.safeCall(() ->
                tourApiClient.searchFestival2(serviceKey, "ETC", "swyp10", "json",
                    pageSize, page, startDate, endDate));

            return batchUtils.parseFestivalList(response);

        } catch (Exception e) {
            log.warn("Failed to fetch festival page {}: {}", page, e.getMessage());
            return List.of();
        }
    }

    private int getTotalCount(String startDate, String endDate, int pageSize) {
        try {
            Map<String, Object> response = tourApiClient.searchFestival2(
                serviceKey, "ETC", "swyp10", "json", 1, 1, startDate, endDate);

            return batchUtils.extractTotalCount(response);

        } catch (Exception e) {
            log.warn("Failed to get total count, using default", e);
            return 1000; // 기본값
        }
    }

    private void processFestivalPage(List<SearchFestival2Dto> festivals, BatchResult result) {
        // 최대 2개까지만 처리
        int limit = Math.min(festivals.size(), 2);

        for (int i = 0; i < limit; i++) {
            SearchFestival2Dto festival = festivals.get(i);
            try {
                processSingleFestival(festival);
                result.incrementSuccess();

                if (result.getTotalProcessed() % 10 == 0) {
                    log.info("Progress: {}", result);
                }

            } catch (Exception e) {
                log.warn("Failed to process festival {}: {}", festival.getContentid(), e.getMessage());
                result.incrementError();
            }
        }
    }


    private void processSingleFestival(SearchFestival2Dto searchDto) {
        String contentId = searchDto.getContentid();

        var commonDto = apiCaller.fetchDetailCommon(contentId);
        var introDto = apiCaller.fetchDetailIntro(contentId);
        var images = apiCaller.fetchDetailImages(contentId);

        festivalService.saveOrUpdateFestival(searchDto, commonDto, introDto, images);
    }
}