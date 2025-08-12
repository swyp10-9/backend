package com.swyp10.domain.travelcourse.batch;

import com.swyp10.domain.travelcourse.service.TravelCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class TravelCourseItemWriter implements ItemWriter<TravelCourseProcessedData> {

    private final TravelCourseService travelCourseService;

    @Override
    public void write(Chunk<? extends TravelCourseProcessedData> chunk) throws Exception {
        log.info("Writing {} travel courses to database", chunk.size());
        
        for (TravelCourseProcessedData data : chunk) {
            if (data != null) {
                try {
                    travelCourseService.saveOrUpdateTravelCourse(
                        data.getSearchDto(),
                        data.getDetailInfo()
                    );
                } catch (Exception e) {
                    log.error("Failed to save travel course {}: {}", 
                        data.getSearchDto().getContentid(), e.getMessage());
                }
            }
        }
        
        log.info("Successfully wrote {} travel courses", chunk.size());
        
        // 메모리 정리
        if (chunk.size() > 0) {
            System.gc();
        }
    }
}
