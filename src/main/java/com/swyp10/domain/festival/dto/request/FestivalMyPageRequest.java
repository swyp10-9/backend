package com.swyp10.domain.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FestivalMyPageRequest extends FestivalPageRequest {
    @Schema(description = "북마크 여부", example = "true")
    private Boolean bookmarked;
}
