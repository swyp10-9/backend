package com.swyp10.domain.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalPageResponse {
    
    @Schema(description = "축제 목록")
    private List<FestivalResponse> content;
    
    @Schema(description = "전체 요소 개수", example = "150")
    private Long totalElements;
    
    @Schema(description = "전체 페이지 개수", example = "8")
    private Integer totalPages;
    
    @Schema(description = "현재 페이지 크기", example = "20")
    private Integer size;
    
    @Schema(description = "현재 페이지 번호", example = "0")
    private Integer number;
    
    @Schema(description = "첫 번째 페이지 여부", example = "true")
    private Boolean first;
    
    @Schema(description = "마지막 페이지 여부", example = "false")
    private Boolean last;
    
    @Schema(description = "빈 페이지 여부", example = "false")
    private Boolean empty;
    
    public static FestivalPageResponse from(Page<FestivalResponse> page) {
        return FestivalPageResponse.builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .number(page.getNumber())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
