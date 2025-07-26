package com.swyp10.domain.festival.dto.request;

import com.swyp10.domain.festival.enums.FestivalPersonalityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FestivalPersonalTestRequest extends FestivalPageRequest {
    @Schema(description = "테스트 성향 결과", example = "축제 에너자이저")
    private FestivalPersonalityType personalityType;
}
