package com.swyp10.global.page;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Offset 기반 페이징 응답을 위한 글로벌 클래스
 * 페이징이 필요한 API의 Response DTO에서 상속받아 사용
 * @param <T> 응답 데이터 타입
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "페이징 응답 정보")
public class PageResponse<T> {

    @Schema(description = "응답 데이터 목록", required = true)
    private List<T> content;

    @Schema(description = "현재 페이지 번호 (0부터 시작)", required = true, example = "0")
    private Integer page;

    @Schema(description = "페이지 크기", required = true, example = "20")
    private Integer size;

    @Schema(description = "전체 요소 개수", required = true, example = "100")
    private Long totalElements;

    @Schema(description = "전체 페이지 개수", required = true, example = "5")
    private Integer totalPages;

    @Schema(description = "첫 번째 페이지 여부", required = true, example = "true")
    private Boolean first;

    @Schema(description = "마지막 페이지 여부", required = true, example = "false")
    private Boolean last;

    @Schema(description = "빈 페이지 여부", required = true, example = "false")
    private Boolean empty;

    /**
     * Spring Data JPA의 Page 객체로부터 PageResponse 객체를 생성
     * @param page Spring Data JPA Page 객체
     * @param <T> 데이터 타입
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * 데이터와 페이징 정보로부터 PageResponse 객체를 생성
     * @param content 데이터 목록
     * @param pageRequest 페이징 요청 정보
     * @param totalElements 전체 요소 개수
     * @param <T> 데이터 타입
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> of(List<T> content, PageRequest pageRequest, Long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.getSize());
        int currentPage = pageRequest.getPage();
        
        return PageResponse.<T>builder()
                .content(content)
                .page(currentPage)
                .size(pageRequest.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(currentPage == 0)
                .last(currentPage >= totalPages - 1)
                .empty(content.isEmpty())
                .build();
    }

    /**
     * 빈 PageResponse 객체를 생성
     * @param pageRequest 페이징 요청 정보
     * @param <T> 데이터 타입
     * @return 빈 PageResponse 객체
     */
    public static <T> PageResponse<T> empty(PageRequest pageRequest) {
        return PageResponse.<T>builder()
                .content(List.of())
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .empty(true)
                .build();
    }
}
