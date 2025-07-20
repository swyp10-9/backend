package com.swyp10.domain.review.service;

import com.swyp10.domain.review.entity.ReviewLike;
import com.swyp10.domain.review.repository.ReviewLikeRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewLikeService 테스트")
class ReviewLikeServiceTest {

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @InjectMocks
    private ReviewLikeService reviewLikeService;

    private ReviewLike reviewLike;

    @BeforeEach
    void setUp() {
        reviewLike = ReviewLike.builder()
            .reviewLikeId(1L)
            .build();
    }

    @Nested
    @DisplayName("ReviewLike 조회")
    class GetReviewLike {

        @Test
        @DisplayName("정상 조회")
        void getReviewLikeSuccess() {
            // given
            given(reviewLikeRepository.findById(1L)).willReturn(Optional.of(reviewLike));

            // when
            ReviewLike result = reviewLikeService.getReviewLike(1L);

            // then
            assertThat(result).isEqualTo(reviewLike);
            verify(reviewLikeRepository).findById(1L);
        }

        @Test
        @DisplayName("조회 실패 시 예외")
        void getReviewLikeFail() {
            // given
            given(reviewLikeRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewLikeService.getReviewLike(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("ReviewLike 생성")
    class CreateReviewLike {

        @Test
        @DisplayName("정상 생성")
        void createReviewLike() {
            // given
            given(reviewLikeRepository.save(reviewLike)).willReturn(reviewLike);

            // when
            ReviewLike result = reviewLikeService.createReviewLike(reviewLike);

            // then
            assertThat(result).isEqualTo(reviewLike);
            verify(reviewLikeRepository).save(reviewLike);
        }
    }

    @Nested
    @DisplayName("ReviewLike 삭제")
    class DeleteReviewLike {

        @Test
        @DisplayName("ID로 삭제")
        void deleteReviewLike() {
            // when
            reviewLikeService.deleteReviewLike(1L);

            // then
            verify(reviewLikeRepository).deleteById(1L);
        }
    }
}
