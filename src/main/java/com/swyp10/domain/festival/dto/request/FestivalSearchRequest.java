package com.swyp10.domain.festival.dto.request;

import com.swyp10.domain.festival.enums.FestivalTheme;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FestivalSearchRequest extends FestivalPageRequest {
    
    @Schema(description = "검색 시작 날짜", required = false, nullable = true, example = "2025-08-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @Schema(description = "검색 종료 날짜", required = false, nullable = true, example = "2025-08-31")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @Schema(description = "경도 (한국 범위: 124~132)", required = false, nullable = true, example = "126.9780")
    private Double mapX;
    
    @Schema(description = "위도 (한국 범위: 33~39)", required = false, nullable = true, example = "37.5665")
    private Double mapY;
    
    @Schema(description = "검색 반경(미터)", required = false, nullable = true, example = "10000")
    private Integer radius;
    
    @Schema(description = "지역 코드", required = false, nullable = true, example = "1")
    private Integer regionCode;
    
    @Schema(description = "축제 테마", required = false, nullable = true, example = "MUSIC")
    private FestivalTheme theme;
    
    @Schema(description = "검색 키워드 (축제명, 설명, 지역명 등 자유 검색)", required = false, nullable = true, example = "벚꽃축제")
    private String searchParam;
}
