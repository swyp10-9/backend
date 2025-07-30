package com.swyp10.domain.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FestivalPageRequest {
    
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    private Integer page = 0;
    
    @Schema(description = "페이지 크기", example = "20")
    private Integer size = 20;
    
    @Schema(description = "정렬 기준 (예: createdAt,desc)", example = "createdAt,desc")
    private String sort;
}
