package com.swyp10.global.page;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Offset 기반 페이징 요청을 위한 글로벌 클래스
 * 페이징이 필요한 API의 Request DTO에서 상속받아 사용
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "페이징 요청 정보")
public class PageRequest {

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    @Builder.Default
    private Integer size = 20;

    /**
     * offset 값을 계산하여 반환
     * @return offset 값
     */
    public long getOffset() {
        return (long) page * size;
    }

    /**
     * Spring Data JPA의 Pageable 객체로 변환
     * @return Pageable 객체
     */
    public org.springframework.data.domain.Pageable toPageable() {
        return org.springframework.data.domain.PageRequest.of(page, size);
    }

    /**
     * 정렬 조건이 있는 Pageable 객체로 변환
     * @param sort 정렬 조건
     * @return Pageable 객체
     */
    public org.springframework.data.domain.Pageable toPageable(org.springframework.data.domain.Sort sort) {
        return org.springframework.data.domain.PageRequest.of(page, size, sort);
    }
}
