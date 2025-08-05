package com.swyp10.domain.festival.dto.response;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.enums.FestivalTheme;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalResponse {
    
    @Schema(description = "축제 ID", required = false, nullable = false, example = "1")
    private Long festivalId;
    
    @Schema(description = "축제명", required = false, nullable = false, example = "여의도 벚꽃축제")
    private String name;
    
    @Schema(description = "축제 시작일", required = false, nullable = true, example = "2025-04-01")
    private LocalDate startDate;
    
    @Schema(description = "축제 종료일", required = false, nullable = true, example = "2025-04-10")
    private LocalDate endDate;
    
    @Schema(description = "축제 테마", required = false, nullable = true, example = "NATURE")
    private FestivalTheme theme;
    
    @Schema(description = "축제 설명", required = false, nullable = true, example = "서울 대표 벚꽃축제로 한강변에서 열리는 봄축제입니다.")
    private String description;
    
    @Schema(description = "썸네일 이미지 URL", required = false, nullable = true, example = "https://example.com/festival_thumbnail.jpg")
    private String thumbnail;
    
    @Schema(description = "위치 정보 (mapX, mapY 포함)", required = false, nullable = true)
    private Map<String, Object> location;
    
    @Schema(description = "지역 정보", required = false, nullable = true)
    private RegionResponse region;
    
    @Schema(description = "생성일시", required = false, nullable = false, example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일시", required = false, nullable = false, example = "2025-01-20T14:20:00")
    private LocalDateTime updatedAt;
    
    // Festival Entity를 FestivalResponse로 변환하는 정적 팩토리 메서드
    public static FestivalResponse from(Festival festival) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        LocalDate startDate = null;
        String eventStartDateStr = festival.getBasicInfo().getEventstartdate();
        if (eventStartDateStr != null && !eventStartDateStr.isBlank()) {
            try {
                startDate = LocalDate.parse(eventStartDateStr, formatter);
            } catch (DateTimeParseException e) {
                log.error("start date parse error", e);
            }
        }

        LocalDate endDate = null;
        String eventEndDateStr = festival.getBasicInfo().getEventenddate();
        if (eventEndDateStr != null && !eventEndDateStr.isBlank()) {
            try {
                endDate = LocalDate.parse(eventEndDateStr, formatter);
            } catch (DateTimeParseException e) {
                log.error("end date parse error", e);
            }
        }

        return FestivalResponse.builder()
                .festivalId(festival.getFestivalId())
                .name(festival.getBasicInfo().getTitle())
                .startDate(startDate)
                .endDate(endDate)
                .description(festival.getOverview())
                .thumbnail(festival.getBasicInfo().getFirstimage())
                .location(Map.of("mapX", festival.getBasicInfo().getMapx(), "mapY", festival.getBasicInfo().getMapy()))
                .region(festival.getRegion() != null ? RegionResponse.from(festival.getRegion()) : null)
                .createdAt(festival.getCreatedAt())
                .updatedAt(festival.getUpdatedAt())
                .build();
    }
}
