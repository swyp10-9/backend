package com.swyp10.domain.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FestivalPageRequest {
    @Schema(description = "페이지 오프셋", example = "0")
    private Integer offset = 0;

    @Schema(description = "페이지 사이즈", example = "20")
    private Integer limit = 20;
}
