package com.swyp10.domain.restaurant.batch;

import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class RestaurantItemReader implements ItemReader<Object> {

    private final TourApiClient tourApiClient;
    private final String serviceKey;
    private final String contentTypeId;
    private final int pageSize;
    private final int maxTotalItems;

    private Queue<AreaBasedList2RestaurantDto> restaurantQueue = new LinkedList<>();
    private int currentPage = 1;
    private Integer totalCount = null;
    private boolean hasMoreData = true;
    private int processedCount = 0;

    @Override
    public Object read() throws Exception {
        // 최대 아이템 수 제한 확인
        if (processedCount >= maxTotalItems) {
            return null;
        }

        // 큐가 비어있고 더 가져올 데이터가 있으면 다음 페이지 로드
        if (restaurantQueue.isEmpty() && hasMoreData) {
            loadNextPage();
        }

        // 큐에서 하나씩 반환
        AreaBasedList2RestaurantDto restaurant = restaurantQueue.poll();
        if (restaurant != null) {
            processedCount++;
        }
        return restaurant;
    }

    private void loadNextPage() {
        try {
            log.info("Loading restaurant page {} (pageSize: {})", currentPage, pageSize);

            Map<String, Object> response = tourApiClient.areaBasedList2(
                serviceKey, "ETC", "swyp10", "json", pageSize, currentPage, 
                contentTypeId, null, null, null, null
            );

            RestaurantBatchUtils batchUtils = new RestaurantBatchUtils(null);
            List<AreaBasedList2RestaurantDto> restaurants = batchUtils.parseRestaurantList(response);

            if (restaurants.isEmpty()) {
                hasMoreData = false;
                log.info("No more restaurants to load");
                return;
            }

            // 첫 페이지에서 총 개수 확인
            if (totalCount == null) {
                totalCount = batchUtils.extractTotalCount(response);
                log.info("Total restaurants available: {}, processing max: {}", totalCount, maxTotalItems);
            }

            // 최대 처리 개수 제한 적용
            int remainingItems = maxTotalItems - processedCount;
            if (restaurants.size() > remainingItems) {
                restaurants = restaurants.subList(0, remainingItems);
                hasMoreData = false;
            }

            restaurantQueue.addAll(restaurants);
            currentPage++;

            // 모든 데이터를 읽었는지 확인
            if ((currentPage - 1) * pageSize >= totalCount || processedCount + restaurantQueue.size() >= maxTotalItems) {
                hasMoreData = false;
            }

            // API 부하 방지
            Thread.sleep(200);

        } catch (Exception e) {
            log.error("Failed to load restaurant page {}: {}", currentPage, e.getMessage());
            hasMoreData = false;
        }
    }
}
