package com.swyp10.domain.festival.batch;

import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class FestivalItemReader implements ItemReader<SearchFestival2Dto> {

    private final TourApiClient tourApiClient;
    private final String serviceKey;
    private final String eventStartDate;
    private final String eventEndDate;
    private final int pageSize;

    private Queue<SearchFestival2Dto> festivalQueue = new LinkedList<>();
    private int currentPage = 1;
    private Integer totalCount = null;
    private boolean hasMoreData = true;

    @Override
    public SearchFestival2Dto read() throws Exception {
        // 큐가 비어있고 더 가져올 데이터가 있으면 다음 페이지 로드
        if (festivalQueue.isEmpty() && hasMoreData) {
            loadNextPage();
        }

        // 큐에서 하나씩 반환 (메모리 절약)
        return festivalQueue.poll();
    }

    private void loadNextPage() {
        try {
            log.info("Loading page {} (pageSize: {})", currentPage, pageSize);

            Map<String, Object> response = tourApiClient.searchFestival2(
                serviceKey, "ETC", "swyp10", "json", 
                pageSize, currentPage, eventStartDate, eventEndDate
            );

            FestivalBatchUtils batchUtils = new FestivalBatchUtils(null);
            List<SearchFestival2Dto> festivals = batchUtils.parseFestivalList(response);

            if (festivals.isEmpty()) {
                hasMoreData = false;
                log.info("No more festivals to load");
                return;
            }

            // 첫 페이지에서 총 개수 확인
            if (totalCount == null) {
                totalCount = batchUtils.extractTotalCount(response);
                log.info("Total festivals to process: {}", totalCount);
            }

            // 큐에 추가 (메모리에는 현재 페이지 데이터만 유지)
            festivalQueue.addAll(festivals);
            
            // 다음 페이지 설정
            currentPage++;
            
            // 모든 데이터를 읽었는지 확인
            if ((currentPage - 1) * pageSize >= totalCount) {
                hasMoreData = false;
                log.info("All pages loaded. Total pages: {}", currentPage - 1);
            }

            // API 부하 방지를 위한 딜레이
            Thread.sleep(200);

        } catch (Exception e) {
            log.error("Failed to load page {}: {}", currentPage, e.getMessage());
            hasMoreData = false;
        }
    }
}
