package com.swyp10.domain.festival.dto.request;

import com.swyp10.global.page.PageRequest;
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
public class FestivalPageRequest extends PageRequest {
    
    @Schema(description = "정렬 기준 (예: createdAt,desc)", required = false, nullable = true, example = "createdAt,desc")
    private String sort;
}
