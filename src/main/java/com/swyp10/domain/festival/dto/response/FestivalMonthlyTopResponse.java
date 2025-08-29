package com.swyp10.domain.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp10.domain.festival.entity.Festival;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@Builder
public class FestivalMonthlyTopResponse {
    @Schema(description = "축제 ID", required = true, nullable = false, example = "1234")
    private Long id;

    @Schema(description = "썸네일 이미지 URL", required = false, nullable = true, example = "https://...")
    private String thumbnail;

    @Schema(description = "테마", required = false, nullable = true, example = "음식/미식")
    private String theme;

    @Schema(description = "축제명", required = true, nullable = false, example = "부산 불꽃축제")
    private String title;

    @Schema(description = "축제 설명", required = true, nullable = false, example = "매년 가을에 열리는...")
    private String overview;

    @Schema(description = "북마크 여부", required = true, nullable = false, example = "true")
    private Boolean bookmarked;

    @Schema(description = "주소", required = false, nullable = true, example = "부산광역시 해운대구")
    private String address;

    @Schema(description = "축제 시작일", required = false, nullable = true, example = "2025-09-01", type = "string", format = "date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "축제 종료일", required = false, nullable = true, example = "2025-09-03", type = "string", format = "date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "경도(X좌표)", required = false, nullable = true, example = "127.5881015063")
    private String map_x;

    @Schema(description = "위도(Y좌표)", required = false, nullable = true, example = "36.9913818048")
    private String map_y;

    public static FestivalMonthlyTopResponse from(Festival festival) {
        return FestivalMonthlyTopResponse.builder()
            .id(festival.getFestivalId())
            .thumbnail(festival.getBasicInfo() != null ? festival.getBasicInfo().getFirstimage2() : null)
            .theme(festival.getTheme() != null ? festival.getTheme().name() : null)
            .title(festival.getBasicInfo() != null ? festival.getBasicInfo().getTitle() : null)
            .overview(festival.getOverview()) // overview 포함
            .bookmarked(false) // 기본값으로 false 설정
            .address(festival.getBasicInfo() != null ? festival.getBasicInfo().getAddr1() : null)
            .startDate(festival.getBasicInfo() != null ? festival.getBasicInfo().getEventstartdate() : null)
            .endDate(festival.getBasicInfo() != null ? festival.getBasicInfo().getEventenddate() : null)
            .map_x(festival.getBasicInfo() != null && festival.getBasicInfo().getMapx() != null ?
                String.valueOf(festival.getBasicInfo().getMapx()) : null)
            .map_y(festival.getBasicInfo() != null && festival.getBasicInfo().getMapy() != null ?
                String.valueOf(festival.getBasicInfo().getMapy()) : null)
            .build();
    }
}
