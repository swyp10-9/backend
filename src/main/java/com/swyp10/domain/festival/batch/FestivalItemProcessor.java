package com.swyp10.domain.festival.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@RequiredArgsConstructor
public class FestivalItemProcessor implements ItemProcessor<SearchFestival2Dto, FestivalProcessedData> {

    private final TourApiClient tourApiClient;
    private final String serviceKey;
    private final ObjectMapper objectMapper;

    private final FestivalApiCaller apiCaller;

    public FestivalItemProcessor(TourApiClient tourApiClient, String serviceKey, ObjectMapper objectMapper) {
        this.tourApiClient = tourApiClient;
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
        this.apiCaller = new FestivalApiCaller(tourApiClient, serviceKey);
    }

    @Override
    public FestivalProcessedData process(SearchFestival2Dto item) throws Exception {
        try {
            String contentId = item.getContentid();
            
            // 상세 정보 가져오기 (메모리에 최소한만 유지)
            var commonDto = apiCaller.fetchDetailCommon(contentId);
            var introDto = apiCaller.fetchDetailIntro(contentId);
            var images = apiCaller.fetchDetailImages(contentId);

            // 처리된 데이터를 하나의 객체로 묶어서 반환
            return new FestivalProcessedData(item, commonDto, introDto, images);

        } catch (Exception e) {
            log.warn("Failed to process festival {}: {}", item.getContentid(), e.getMessage());
            // null 반환하면 해당 아이템은 건너뛰어짐
            return null;
        }
    }
}
