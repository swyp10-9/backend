package com.swyp10.domain.festival.batch;

import com.swyp10.domain.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class FestivalItemWriter implements ItemWriter<FestivalProcessedData> {

    private final FestivalService festivalService;

    @Override
    public void write(Chunk<? extends FestivalProcessedData> chunk) throws Exception {
        log.info("Writing {} festivals to database", chunk.size());
        
        for (FestivalProcessedData data : chunk) {
            if (data != null) {
                try {
                    // 배치로 저장 (트랜잭션으로 묶임)
                    festivalService.saveOrUpdateFestival(
                        data.getSearchDto(),
                        data.getCommonDto(),
                        data.getIntroDto(),
                        data.getImages()
                    );
                } catch (Exception e) {
                    log.error("Failed to save festival {}: {}", 
                        data.getSearchDto().getContentid(), e.getMessage());
                    // 개별 실패는 전체 처리를 중단하지 않음
                }
            }
        }
        
        log.info("Successfully wrote {} festivals", chunk.size());
        
        // 메모리 정리를 위한 가비지 컬렉션 힌트
        if (chunk.size() > 0) {
            System.gc();
        }
    }
}
