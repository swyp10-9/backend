package com.swyp10.domain.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp10.domain.festival.entity.Festival;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FestivalSummaryResponse {
    @Schema(description = "축제 ID", required = true, nullable = false, example = "1234")
    private Long id;

    @Schema(description = "썸네일 이미지 URL", required = false, nullable = true, example = "https://...")
    private String thumbnail;

    @Schema(description = "테마", required = false, nullable = true, example = "음식/미식")
    private String theme;

    @Schema(description = "축제명", required = true, nullable = false, example = "부산 불꽃축제")
    private String title;

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

    public static FestivalSummaryResponse from(Festival festival) {
        return FestivalSummaryResponse.builder()
            .id(Long.parseLong(festival.getContentId()))
            .thumbnail(festival.getBasicInfo().getFirstimage2())
            .theme(festival.getTheme() != null ? festival.getTheme().getDisplayName() : null)
            .title(festival.getBasicInfo().getTitle())
            .bookmarked(false) // todo bookmarked 설정 필요
            .address(festival.getBasicInfo().getAddr1())
            .startDate(festival.getBasicInfo().getEventstartdate())
            .endDate(festival.getBasicInfo().getEventenddate())
            .map_x(String.valueOf(festival.getBasicInfo().getMapx()))
            .map_y(String.valueOf(festival.getBasicInfo().getMapy()))
            .build();
    }
}
