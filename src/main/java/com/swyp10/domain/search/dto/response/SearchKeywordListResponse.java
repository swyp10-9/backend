package com.swyp10.domain.search.dto.response;

import com.swyp10.global.page.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Schema(description = "검색 키워드 목록 응답")
public class SearchKeywordListResponse extends PageResponse<SearchKeywordResponse> {
    // 추가적인 필드가 필요한 경우 여기에 추가
}
