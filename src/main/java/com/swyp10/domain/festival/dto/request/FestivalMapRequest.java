package com.swyp10.domain.festival.dto.request;

import com.swyp10.domain.festival.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FestivalMapRequest extends FestivalPageRequest {

    @Schema(description = "축제 상태", example = "ONGOING")
    private FestivalStatus status = FestivalStatus.ALL;

    @Schema(description = "기간", example = "THIS_WEEK")
    private FestivalPeriod period = FestivalPeriod.ALL;

    @Schema(description = "누구랑", example = "FAMILY")
    private FestivalWithWhom withWhom = FestivalWithWhom.ALL;

    @Schema(description = "테마", example = "CULTURE_ART")
    private FestivalTheme theme = FestivalTheme.ALL;

    @Schema(description = "좌상단 위도", example = "37.6")
    private Double latTopLeft;

    @Schema(description = "좌상단 경도", example = "126.9")
    private Double lngTopLeft;

    @Schema(description = "우하단 위도", example = "37.4")
    private Double latBottomRight;

    @Schema(description = "우하단 경도", example = "127.1")
    private Double lngBottomRight;
}
