package com.swyp10.domain.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class FestivalDailyCountResponse {
    @Schema(description = "조회 시작일", example = "2025-08-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "조회 종료일", example = "2025-08-31")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "일자별 진행 중 축제 개수 리스트")
    private List<DailyCount> dailyCounts;

    @Getter
    @AllArgsConstructor
    public static class DailyCount {
        @Schema(description = "날짜", example = "2025-08-01")
        private LocalDate date;
        @Schema(description = "해당 날짜에 진행중인 축제 개수", example = "5")
        private int count;
    }
}