package com.swyp10.domain.bookmark.repository;

import com.swyp10.config.QueryDslConfig;
import com.swyp10.config.TestConfig;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.enums.FestivalStatus;
import com.swyp10.domain.festival.repository.FestivalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserBookmarkCustomRepositoryImpl 통합 테스트
 */
@DataJpaTest
@Import({TestConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
@EntityScan(basePackages = "com.swyp10.domain")
@DisplayName("UserBookmarkCustomRepositoryImpl 통합 테스트")
class UserBookmarkCustomRepositoryImplTest {

    @Autowired
    private UserBookmarkRepository userBookmarkRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private UserRepository userRepository;

    private Festival saveFestival(String contentId, String title) {
        FestivalBasicInfo basic = FestivalBasicInfo.builder()
            .title(title)
            .eventstartdate(LocalDate.now().minusDays(1))
            .eventenddate(LocalDate.now().plusDays(1))
            .mapx(127.0)
            .mapy(37.5)
            .addr1("서울시 어딘가")
            .build();

        Festival f = Festival.builder()
            .contentId(contentId)
            .basicInfo(basic)
            .status(FestivalStatus.ONGOING)
            .build();
        return festivalRepository.save(f);
    }

    private User saveUser(String email) {
        return userRepository.save(
            User.builder()
                .email(email)
                .password("pw")
                .nickname("nick")
                .signupCompleted(true)
                .build()
        );
    }

    private UserBookmark bookmark(User user, Festival festival, boolean deleted) {
        UserBookmark ub = UserBookmark.builder()
            .user(user)
            .festival(festival)
            .createdAt(LocalDateTime.now().minusMinutes(1))
            .build();
        if (deleted) {
            ub.markDeleted();
        }
        return userBookmarkRepository.save(ub);
    }

    @Test
    @DisplayName("내 북마크 목록 조회 - 성공(2건, 최신순)")
    void findBookmarkedFestivals_success() {
        // given
        User u = saveUser("u@a.com");
        Festival f1 = saveFestival("11111", "축제1");
        Festival f2 = saveFestival("22222", "축제2");

        bookmark(u, f1, false);
        // 최근 북마크 효과 주기
        UserBookmark last = bookmark(u, f2, false);
        last = userBookmarkRepository.save(last);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"))); // impl에서 createdAt desc 사용

        // when
        Page<FestivalSummaryResponse> page =
            userBookmarkRepository.findBookmarkedFestivals(u.getUserId(), pageable);

        // then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting("title").containsExactlyInAnyOrder("축제1", "축제2");
    }

    @Test
    @DisplayName("삭제된 북마크는 제외 - 성공")
    void findBookmarkedFestivals_ignoreDeleted() {
        User u = saveUser("u@b.com");
        Festival f1 = saveFestival("11111", "축제11");
        Festival f2 = saveFestival("22222", "축제22");

        bookmark(u, f1, false);
        bookmark(u, f2, true); // 삭제됨

        Pageable pageable = PageRequest.of(0, 10);

        Page<FestivalSummaryResponse> page =
            userBookmarkRepository.findBookmarkedFestivals(u.getUserId(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("축제11");
    }

    @Test
    @DisplayName("북마크 없음 - 빈 결과")
    void findBookmarkedFestivals_empty() {
        User u = saveUser("empty@x.com");
        Pageable pageable = PageRequest.of(0, 10);

        Page<FestivalSummaryResponse> page =
            userBookmarkRepository.findBookmarkedFestivals(u.getUserId(), pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
        assertThat(page.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("페이징 동작 확인 - page=0 size=1")
    void findBookmarkedFestivals_paging() {
        User u = saveUser( "page@x.com");
        Festival f1 = saveFestival("11111", "축제101");
        Festival f2 = saveFestival("22222", "축제102");

        bookmark(u, f1, false);
        bookmark(u, f2, false);

        Pageable pageable = PageRequest.of(0, 1);

        Page<FestivalSummaryResponse> page =
            userBookmarkRepository.findBookmarkedFestivals(u.getUserId(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}
