package com.swyp10.domain.festival.dto.request;

import com.swyp10.domain.festival.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
public class FestivalCalendarRequest extends FestivalPageRequest {

    @Schema(description = "지역 필터", example = "서울", allowableValues = {"서울", "경기", "강원", "충청", "전라", "경상", "제주", "전체"})
    private RegionFilter region;

    @Schema(description = "누구랑", example = "가족", allowableValues = {"가족", "커플", "부모님", "반려견", "친구", "전체"})
    private FestivalWithWhom withWhom = FestivalWithWhom.ALL;

    @Schema(description = "테마", example = "음식/미식", allowableValues = {"문화/예술", "음식/미식", "음악/공연", "자연/체험", "전통/역사", "전체"})
    private FestivalTheme theme = FestivalTheme.ALL;

    @Schema(description = "조회 기준 날짜", example = "2025-08-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date = LocalDate.now(); // default 오늘
}
