package com.swyp10.domain.festival.dto.response;

import com.swyp10.global.page.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "축제 페이징 응답")
public class FestivalPageResponse extends PageResponse<FestivalResponse> {
    
    public static FestivalPageResponse from(Page<FestivalResponse> page) {
        FestivalPageResponse response = FestivalPageResponse.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
        return response;
    }
}
