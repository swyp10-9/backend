package com.swyp10.domain.review.service;

import com.swyp10.domain.review.entity.UserReview;
import com.swyp10.domain.review.repository.UserReviewRepository;
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
@DisplayName("UserReviewService 테스트")
class UserReviewServiceTest {

    @Mock
    private UserReviewRepository userReviewRepository;

    @InjectMocks
    private UserReviewService userReviewService;

    private UserReview review;

    @BeforeEach
    void setUp() {
        review = UserReview.builder()
            .id(1L)
            .rating(4)
            .content("좋은 축제였어요.")
            .build();
    }

    @Nested
    @DisplayName("UserReview 조회")
    class GetUserReview {

        @Test
        @DisplayName("정상 조회")
        void getUserReviewSuccess() {
            // given
            given(userReviewRepository.findById(1L)).willReturn(Optional.of(review));

            // when
            UserReview result = userReviewService.getUserReview(1L);

            // then
            assertThat(result).isEqualTo(review);
            verify(userReviewRepository).findById(1L);
        }

        @Test
        @DisplayName("리뷰 없음 - 예외 발생")
        void getUserReviewFail() {
            // given
            given(userReviewRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userReviewService.getUserReview(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("UserReview 생성")
    class CreateUserReview {

        @Test
        @DisplayName("정상 생성")
        void createUserReview() {
            // given
            given(userReviewRepository.save(review)).willReturn(review);

            // when
            UserReview result = userReviewService.createUserReview(review);

            // then
            assertThat(result).isEqualTo(review);
            verify(userReviewRepository).save(review);
        }
    }

    @Nested
    @DisplayName("UserReview 삭제")
    class DeleteUserReview {

        @Test
        @DisplayName("ID로 삭제")
        void deleteUserReview() {
            // when
            userReviewService.deleteUserReview(1L);

            // then
            verify(userReviewRepository).deleteById(1L);
        }
    }
}