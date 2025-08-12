package com.swyp10.domain.travelcourse.batch;

import com.swyp10.domain.festival.client.TourApiClient;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class TravelCourseItemReader implements ItemReader<Object> {

    private final TourApiClient tourApiClient;
    private final String serviceKey;
    private final String contentTypeId;
    private final int pageSize;
    private final int maxTotalItems;

    private Queue<SearchTravelCourseDto> travelCourseQueue = new LinkedList<>();
    private int currentPage = 1;
    private Integer totalCount = null;
    private boolean hasMoreData = true;
    private int processedCount = 0;

    @Override
    public Object read() throws Exception {
        if (processedCount >= maxTotalItems) {
            return null;
        }

        if (travelCourseQueue.isEmpty() && hasMoreData) {
            loadNextPage();
        }

        SearchTravelCourseDto travelCourse = travelCourseQueue.poll();
        if (travelCourse != null) {
            processedCount++;
        }
        return travelCourse;
    }

    private void loadNextPage() {
        try {
            log.info("Loading travel course page {} (pageSize: {})", currentPage, pageSize);

            Map<String, Object> response = tourApiClient.areaBasedList2(
                serviceKey, "ETC", "swyp10", "json", pageSize, currentPage, 
                contentTypeId, null, null, null, null
            );

            TravelCourseBatchUtils batchUtils = new TravelCourseBatchUtils(null);
            List<SearchTravelCourseDto> travelCourses = batchUtils.parseTravelCourseList(response);

            if (travelCourses.isEmpty()) {
                hasMoreData = false;
                log.info("No more travel courses to load");
                return;
            }

            if (totalCount == null) {
                totalCount = batchUtils.extractTotalCount(response);
                log.info("Total travel courses available: {}, processing max: {}", totalCount, maxTotalItems);
            }

            int remainingItems = maxTotalItems - processedCount;
            if (travelCourses.size() > remainingItems) {
                travelCourses = travelCourses.subList(0, remainingItems);
                hasMoreData = false;
            }

            travelCourseQueue.addAll(travelCourses);
            currentPage++;

            if ((currentPage - 1) * pageSize >= totalCount || processedCount + travelCourseQueue.size() >= maxTotalItems) {
                hasMoreData = false;
            }

            Thread.sleep(200);

        } catch (Exception e) {
            log.error("Failed to load travel course page {}: {}", currentPage, e.getMessage());
            hasMoreData = false;
        }
    }
}
