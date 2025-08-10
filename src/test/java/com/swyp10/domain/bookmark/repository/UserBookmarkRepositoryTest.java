package com.swyp10.domain.bookmark.repository;

import com.swyp10.config.QueryDslConfig;
import com.swyp10.config.TestConfig;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = "com.swyp10.domain")
@Import({TestConfig.class, QueryDslConfig.class})
@DisplayName("UserBookmarkRepository 테스트")
class UserBookmarkRepositoryTest {

    @Autowired
    UserBookmarkRepository userBookmarkRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FestivalRepository festivalRepository;

    private User saveUser() {
        return userRepository.save(
            User.builder()
                .email("user@test.com")
                .password("pw")
                .nickname("닉")
                .signupCompleted(true)
                .build()
        );
    }

    private Festival saveFestival() {
        return festivalRepository.save(
            Festival.builder()
                .contentId("1111")
                .build()
        );
    }

    @Test
    @DisplayName("활성 북마크 조회 성공")
    void findActiveBookmark_success() {
        User user = saveUser();
        Festival festival = saveFestival();

        UserBookmark ub = userBookmarkRepository.save(
            UserBookmark.builder()
                .user(user)
                .festival(festival)
                .build()
        );

        Optional<UserBookmark> found = userBookmarkRepository
            .findByUser_UserIdAndFestival_ContentIdAndDeletedAtIsNull(user.getUserId(), String.valueOf(festival.getContentId()));

        assertThat(found).isPresent();
        assertThat(found.get().getBookmarkId()).isEqualTo(ub.getBookmarkId());
        assertThat(found.get().getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("활성 북마크 존재 여부 - true/false")
    void existsActiveBookmark() {
        User user = saveUser();
        Festival festival = saveFestival();
        String contentId = String.valueOf(festival.getContentId());

        // 없을 때 false
        boolean before = userBookmarkRepository
            .existsByUser_UserIdAndFestival_ContentIdAndDeletedAtIsNull(user.getUserId(), contentId);
        assertThat(before).isFalse();

        // 생성하면 true
        userBookmarkRepository.save(
            UserBookmark.builder().user(user).festival(festival).build()
        );
        boolean after = userBookmarkRepository
            .existsByUser_UserIdAndFestival_ContentIdAndDeletedAtIsNull(user.getUserId(), contentId);
        assertThat(after).isTrue();
    }

    @Test
    @DisplayName("soft delete 후 활성 조회 불가")
    void softDelete_makesItInactive() {
        User user = saveUser();
        Festival festival = saveFestival();

        UserBookmark ub = userBookmarkRepository.save(
            UserBookmark.builder().user(user).festival(festival).build()
        );

        // soft delete
        ub.markDeleted();
        userBookmarkRepository.save(ub);

        Optional<UserBookmark> found = userBookmarkRepository
            .findByUser_UserIdAndFestival_ContentIdAndDeletedAtIsNull(user.getUserId(), String.valueOf(festival.getContentId()));

        assertThat(found).isEmpty();

        // 원본은 deletedAt 값이 존재
        UserBookmark raw = userBookmarkRepository.findById(ub.getBookmarkId()).orElseThrow();
        assertThat(raw.getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
