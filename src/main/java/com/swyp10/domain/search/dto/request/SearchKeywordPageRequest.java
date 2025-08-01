package com.swyp10.domain.search.dto.request;

import com.swyp10.global.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "검색어 목록 조회 요청")
public class SearchKeywordPageRequest extends PageRequest {
    
    @Schema(description = "검색 기간 시작일", required = false, nullable = true, example = "2024-01-01")
    private String startDate;
    
    @Schema(description = "검색 기간 종료일", required = false, nullable = true, example = "2024-12-31")
    private String endDate;
    
    @Schema(description = "정렬 기준 (searchCount,desc | keyword,asc)", required = false, nullable = true, example = "searchCount,desc")
    private String sort;
}
