package com.swyp10.domain.festival.dto.request;

import com.swyp10.domain.festival.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
public class FestivalCalendarRequest extends FestivalPageRequest {

    @Schema(description = "지역 필터", required = false, nullable = true, example = "SEOUL")
    private RegionFilter region;

    @Schema(description = "누구랑", required = false, nullable = false, example = "FAMILY")
    private FestivalWithWhom withWhom = FestivalWithWhom.ALL;

    @Schema(description = "테마", required = false, nullable = false, example = "CULTURE_ART")
    private FestivalTheme theme = FestivalTheme.ALL;

    @Schema(description = "조회 기준 날짜", required = false, nullable = false, example = "2025-08-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date = LocalDate.now(); // default 오늘
}
