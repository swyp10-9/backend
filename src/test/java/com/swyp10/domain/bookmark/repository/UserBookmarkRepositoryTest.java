package com.swyp10.domain.bookmark.repository;

import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserBookmarkRepository 테스트")
class UserBookmarkRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private UserBookmarkRepository userBookmarkRepository;

    private User user;
    private Festival festival;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .email("test@example.com")
            .password("pass")
            .nickname("유저")
            .signupCompleted(true)
            .build();
        entityManager.persist(user);

        festival = Festival.builder()
            .name("테스트 축제")
            .description("설명")
            .thumbnail("image.jpg")
            .startDate(java.time.LocalDate.now())
            .endDate(java.time.LocalDate.now().plusDays(1))
            .build();
        entityManager.persist(festival);
    }

    @Test
    @DisplayName("북마크 저장 및 조회")
    void save_and_find() {
        UserBookmark bookmark = UserBookmark.builder()
            .user(user)
            .festival(festival)
            .build();
        UserBookmark saved = userBookmarkRepository.save(bookmark);
        entityManager.flush();

        Optional<UserBookmark> found = userBookmarkRepository.findById(saved.getBookmarkId());

        assertThat(found).isPresent();
        assertThat(found.get().getFestival().getName()).isEqualTo("테스트 축제");
        assertThat(found.get().getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("북마크 삭제")
    void delete() {
        UserBookmark bookmark = UserBookmark.builder()
            .user(user)
            .festival(festival)
            .build();
        userBookmarkRepository.save(bookmark);
        entityManager.flush();

        userBookmarkRepository.delete(bookmark);
        entityManager.flush();

        assertThat(userBookmarkRepository.findAll()).isEmpty();
    }
}
