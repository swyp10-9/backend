package com.swyp10.domain.restaurant.batch;

import com.swyp10.domain.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class RestaurantItemWriter implements ItemWriter<RestaurantProcessedData> {

    private final RestaurantService restaurantService;

    @Override
    public void write(Chunk<? extends RestaurantProcessedData> chunk) throws Exception {
        log.info("Writing {} restaurants to database", chunk.size());
        
        for (RestaurantProcessedData data : chunk) {
            if (data != null) {
                try {
                    restaurantService.saveOrUpdateRestaurant(
                        data.getAreaBasedDto(),
                        data.getDetailInfo(),
                        data.getDetailIntro()
                    );
                } catch (Exception e) {
                    log.error("Failed to save restaurant {}: {}", 
                        data.getAreaBasedDto().getContentid(), e.getMessage());
                }
            }
        }
        
        log.info("Successfully wrote {} restaurants", chunk.size());
        
        // 메모리 정리
        if (chunk.size() > 0) {
            System.gc();
        }
    }
}
