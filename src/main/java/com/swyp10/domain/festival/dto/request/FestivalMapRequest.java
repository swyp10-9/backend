package com.swyp10.domain.festival.dto.request;

import com.swyp10.domain.festival.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FestivalMapRequest extends FestivalPageRequest {

    @Schema(description = "축제 상태", example = "진행중", allowableValues = {"진행중", "예정", "전체"})
    private FestivalStatus status = FestivalStatus.ALL;

    @Schema(description = "기간", example = "이번주", allowableValues = {"이번주", "이번달", "다음달", "전체"})
    private FestivalPeriod period = FestivalPeriod.ALL;

    @Schema(description = "누구랑", example = "가족", allowableValues = {"가족", "커플", "부모님", "반려견", "친구", "전체"})
    private FestivalWithWhom withWhom = FestivalWithWhom.ALL;

    @Schema(description = "테마", example = "음식/미식", allowableValues = {"문화/예술", "음식/미식", "음악/공연", "자연/체험", "전통/역사", "전체"})
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
