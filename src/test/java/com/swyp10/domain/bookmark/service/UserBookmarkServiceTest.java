package com.swyp10.domain.bookmark.service;

import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserBookmarkService 테스트")
class UserBookmarkServiceTest {

    @Mock private UserBookmarkRepository userBookmarkRepository;
    @InjectMocks private UserBookmarkService userBookmarkService;

    private UserBookmark testBookmark;

    @BeforeEach
    void setUp() {
        testBookmark = UserBookmark.builder()
            .bookmarkId(1L)
            .build();
    }

    @Nested
    @DisplayName("북마크 조회")
    class GetBookmark {

        @Test
        @DisplayName("북마크 조회 성공")
        void get_success() {
            given(userBookmarkRepository.findById(1L)).willReturn(Optional.of(testBookmark));

            UserBookmark result = userBookmarkService.getUserBookmark(1L);

            assertThat(result).isEqualTo(testBookmark);
            verify(userBookmarkRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 북마크 조회 시 예외 발생")
        void get_not_found() {
            given(userBookmarkRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userBookmarkService.getUserBookmark(99L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("북마크 생성 성공")
    void create_success() {
        given(userBookmarkRepository.save(testBookmark)).willReturn(testBookmark);

        UserBookmark result = userBookmarkService.createUserBookmark(testBookmark);

        assertThat(result).isEqualTo(testBookmark);
        verify(userBookmarkRepository).save(testBookmark);
    }

    @Test
    @DisplayName("북마크 삭제 성공")
    void delete_success() {
        willDoNothing().given(userBookmarkRepository).deleteById(1L);

        userBookmarkService.deleteUserBookmark(1L);

        verify(userBookmarkRepository).deleteById(1L);
    }
}
