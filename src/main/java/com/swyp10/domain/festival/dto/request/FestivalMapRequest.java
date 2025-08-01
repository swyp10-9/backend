package com.swyp10.domain.festival.dto.request;

import com.swyp10.domain.festival.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FestivalMapRequest extends FestivalPageRequest {

    @Schema(description = "축제 상태", required = false, nullable = false, example = "ONGOING")
    private FestivalStatus status = FestivalStatus.ALL;

    @Schema(description = "기간", required = false, nullable = false, example = "THIS_WEEK")
    private FestivalPeriod period = FestivalPeriod.ALL;

    @Schema(description = "누구랑", required = false, nullable = false, example = "FAMILY")
    private FestivalWithWhom withWhom = FestivalWithWhom.ALL;

    @Schema(description = "테마", required = false, nullable = false, example = "CULTURE_ART")
    private FestivalTheme theme = FestivalTheme.ALL;

    @Schema(description = "좌상단 위도", required = false, nullable = true, example = "37.6")
    private Double latTopLeft;

    @Schema(description = "좌상단 경도", required = false, nullable = true, example = "126.9")
    private Double lngTopLeft;

    @Schema(description = "우하단 위도", required = false, nullable = true, example = "37.4")
    private Double latBottomRight;

    @Schema(description = "우하단 경도", required = false, nullable = true, example = "127.1")
    private Double lngBottomRight;
}
