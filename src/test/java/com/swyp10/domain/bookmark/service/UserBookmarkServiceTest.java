package com.swyp10.domain.bookmark.service;

import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserBookmarkService 테스트")
class UserBookmarkServiceTest {

    @Mock UserBookmarkRepository bookmarkRepository;
    @Mock FestivalRepository festivalRepository;
    @Mock UserRepository userRepository;

    @InjectMocks UserBookmarkService userBookmarkService;

    private final Long userId = 10L;
    private final Long festivalId = 100L;

    private User user() { return User.builder().userId(userId).build(); }
    private Festival festival() { return Festival.builder().festivalId(festivalId).build(); }

    @Nested
    @DisplayName("addBookmark API 테스트")
    class AddBookmark {

        @Test
        @DisplayName("신규 북마크 추가 - 성공")
        void addBookmark_success_new() {
            // given
            when(festivalRepository.findByContentId(String.valueOf(festivalId))).thenReturn(Optional.of(festival()));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user()));
            when(bookmarkRepository.findByUser_UserIdAndFestival_FestivalId(userId, festivalId))
                .thenReturn(Optional.empty());
            when(bookmarkRepository.save(any(UserBookmark.class)))
                .thenAnswer(inv -> {
                    UserBookmark ub = inv.getArgument(0);
                    return UserBookmark.builder()
                        .bookmarkId(999L)
                        .user(ub.getUser())
                        .festival(ub.getFestival())
                        .createdAt(LocalDateTime.now())
                        .build();
                });

            // when
            Long savedId = userBookmarkService.addBookmark(userId, festivalId);

            // then
            assertThat(savedId).isEqualTo(999L);
            verify(bookmarkRepository).save(any(UserBookmark.class));
        }

        @Test
        @DisplayName("이미 북마크(삭제 안됨) → BOOKMARK_ALREADY_EXISTS 예외")
        void addBookmark_conflict_alreadyExists() {
            // given
            when(festivalRepository.findByContentId(String.valueOf(festivalId))).thenReturn(Optional.of(festival()));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user()));
            UserBookmark existing = UserBookmark.builder()
                .bookmarkId(1L)
                .user(user())
                .festival(festival())
                .createdAt(LocalDateTime.now())
                .build(); // deletedAt == null
            when(bookmarkRepository.findByUser_UserIdAndFestival_FestivalId(userId, festivalId))
                .thenReturn(Optional.of(existing));

            // expect
            assertThatThrownBy(() -> userBookmarkService.addBookmark(userId, festivalId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.BOOKMARK_ALREADY_EXISTS.getMessage())
                .satisfies(ex ->
                    assertThat(((ApplicationException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BOOKMARK_ALREADY_EXISTS)
                );

            verify(bookmarkRepository, never()).save(any());
        }

        @Test
        @DisplayName("soft-delete 상태 → 복구 성공")
        void addBookmark_revive_softDeleted() throws Exception {
            // given
            when(festivalRepository.findByContentId(String.valueOf(festivalId))).thenReturn(Optional.of(festival()));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user()));

            UserBookmark softDeleted = UserBookmark.builder()
                .bookmarkId(2L)
                .user(user())
                .festival(festival())
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();
            // 강제로 삭제 상태 시뮬레이션
            var field = UserBookmark.class.getDeclaredField("deletedAt");
            field.setAccessible(true);
            field.set(softDeleted, LocalDateTime.now().minusDays(1));

            when(bookmarkRepository.findByUser_UserIdAndFestival_FestivalId(userId, festivalId))
                .thenReturn(Optional.of(softDeleted));

            // when
            Long id = userBookmarkService.addBookmark(userId, festivalId);

            // then: reviveBookmark가 deletedAt을 null로 만든다(리플렉션 내부에서)
            assertThat(id).isEqualTo(2L);
            // save 호출 없이 복구만 하므로 save 호출 안되는게 정상
            verify(bookmarkRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 축제 → FESTIVAL_NOT_FOUND")
        void addBookmark_festivalNotFound() {
            when(festivalRepository.findByContentId(String.valueOf(festivalId))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userBookmarkService.addBookmark(userId, festivalId))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex ->
                    assertThat(((ApplicationException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FESTIVAL_NOT_FOUND)
                );
        }

        @Test
        @DisplayName("존재하지 않는 사용자 → USER_NOT_FOUND")
        void addBookmark_userNotFound() {
            when(festivalRepository.findByContentId(String.valueOf(festivalId))).thenReturn(Optional.of(festival()));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userBookmarkService.addBookmark(userId, festivalId))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex ->
                    assertThat(((ApplicationException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND)
                );
        }
    }
}
