package com.swyp10.domain.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FestivalMyPageRequest extends FestivalPageRequest {
    @Schema(description = "북마크 여부", required = false, nullable = true, example = "true")
    private Boolean bookmarked;
}
