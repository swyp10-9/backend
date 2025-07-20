package com.swyp10.domain.bookmark.entity;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserBookmark Entity 테스트")
class UserBookmarkTest {

    @Test
    @DisplayName("UserBookmark 엔티티 생성")
    void createBookmark() {
        Festival festival = Festival.builder()
            .id(100L)
            .name("대구 치맥페스티벌")
            .startDate(LocalDate.of(2025, 7, 18))
            .endDate(LocalDate.of(2025, 7, 21))
            .build();

        User user = User.builder()
            .userId(1L)
            .email("user@example.com")
            .nickname("북마커")
            .build();

        UserBookmark bookmark = UserBookmark.builder()
            .festival(festival)
            .user(user)
            .build();

        bookmark.onCreate();  // PrePersist 수동 호출

        assertThat(bookmark.getFestival()).isEqualTo(festival);
        assertThat(bookmark.getUser()).isEqualTo(user);
        assertThat(bookmark.getCreatedAt()).isNotNull();
        assertThat(bookmark.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("UserBookmark 엔티티 soft delete")
    void softDeleteBookmark() {
        UserBookmark bookmark = UserBookmark.builder().build();

        assertThat(bookmark.getDeletedAt()).isNull();

        bookmark.markDeleted();

        assertThat(bookmark.getDeletedAt()).isNotNull();
        assertThat(bookmark.isDeleted()).isTrue();
    }
}
