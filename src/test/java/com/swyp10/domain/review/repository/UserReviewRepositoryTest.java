package com.swyp10.domain.review.repository;

import com.swyp10.config.QueryDslConfig;
import com.swyp10.config.TestConfig;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.domain.review.entity.UserReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = "com.swyp10.domain")
@Import({TestConfig.class, QueryDslConfig.class})
@DisplayName("UserReviewRepository 테스트")
class UserReviewRepositoryTest {

    @Autowired UserReviewRepository userReviewRepository;
    @Autowired UserRepository userRepository;
    @Autowired FestivalRepository festivalRepository;

    @Nested
    @DisplayName("findByFestival_ContentIdOrderByCreatedAtDesc")
    class FindByFestivalContent {

        @Test
        @DisplayName("리뷰가 여러 건 있을 때 최신순으로 페이징 조회 - 성공")
        void findPaged_sortedDesc_success() throws Exception {
            // given
            Festival festival = saveFestivalWithContentId("1111", "벚꽃축제");
            User u1 = saveUser("a@test.com", "A");
            User u2 = saveUser("b@test.com", "B");

            // createdAt 차이를 주기 위해 잠깐 sleep
            saveReview(u1, festival, "리뷰1"); Thread.sleep(5);
            saveReview(u2, festival, "리뷰2"); Thread.sleep(5);
            saveReview(u1, festival, "리뷰3(최신)");

            // when
            Page<UserReview> page1 = userReviewRepository
                .findByFestival_ContentIdOrderByCreatedAtDesc("1111", PageRequest.of(0, 2));
            Page<UserReview> page2 = userReviewRepository
                .findByFestival_ContentIdOrderByCreatedAtDesc("1111", PageRequest.of(1, 2));

            // then (page1: 최신 2개)
            assertThat(page1.getTotalElements()).isEqualTo(3);
            assertThat(page1.getContent()).hasSize(2);
            assertThat(page1.getContent().get(0).getContent()).isEqualTo("리뷰3(최신)");
            assertThat(page1.getContent().get(1).getContent()).isEqualTo("리뷰2");

            // then (page2: 남은 1개)
            assertThat(page2.getContent()).hasSize(1);
            assertThat(page2.getContent().get(0).getContent()).isEqualTo("리뷰1");
        }

        @Test
        @DisplayName("해당 축제(contentId)에 리뷰가 없으면 빈 페이지 반환")
        void empty_whenNoReviews() {
            // given
            saveFestivalWithContentId("1111", "조용한축제");

            // when
            Page<UserReview> page = userReviewRepository
                .findByFestival_ContentIdOrderByCreatedAtDesc("1111", PageRequest.of(0, 10));

            // then
            assertThat(page.getTotalElements()).isZero();
            assertThat(page.getContent()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 contentId로 조회 시 빈 페이지")
        void empty_whenContentIdNotExist() {
            // when
            Page<UserReview> page = userReviewRepository
                .findByFestival_ContentIdOrderByCreatedAtDesc("NOT_EXIST", PageRequest.of(0, 5));

            // then
            assertThat(page.getTotalElements()).isZero();
            assertThat(page.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUser_UserIdAndFestival_ContentId")
    class ExistsByUserAndFestivalContent {

        @Test
        @DisplayName("리뷰가 존재할 때 true, 없을 때 false")
        void exists_true_false() {
            // given
            User user = saveUser("exist@test.com", "익지");
            Festival f1 = saveFestivalWithContentId("1111", "후기있는축제");
            Festival f2 = saveFestivalWithContentId("1112", "후기없는축제");

            saveReview(user, f1, "있음!");

            // when
            boolean exists1 = userReviewRepository
                .existsByUser_UserIdAndFestival_ContentId(user.getUserId(), "1111");
            boolean exists2 = userReviewRepository
                .existsByUser_UserIdAndFestival_ContentId(user.getUserId(), "1112");
            boolean existsWrongUser = userReviewRepository
                .existsByUser_UserIdAndFestival_ContentId(999999L, "1111");

            // then
            assertThat(exists1).isTrue();
            assertThat(exists2).isFalse();
            assertThat(existsWrongUser).isFalse();
        }

        @Test
        @DisplayName("contentId 대소문자/공백 등 엣지 입력 시 false (정확 매칭)")
        void exists_edge_exactMatch() {
            // given
            User user = saveUser("edge@test.com", "엣지");
            Festival f = saveFestivalWithContentId("1111", "정확매칭축제");
            saveReview(user, f, "정확하게!");

            // when
            boolean exact = userReviewRepository
                .existsByUser_UserIdAndFestival_ContentId(user.getUserId(), "1111");
            boolean mismatchCase = userReviewRepository
                .existsByUser_UserIdAndFestival_ContentId(user.getUserId(), "1112");
            boolean mismatchSpaces = userReviewRepository
                .existsByUser_UserIdAndFestival_ContentId(user.getUserId(), " 1111 ");

            // then
            assertThat(exact).isTrue();
            assertThat(mismatchCase).isFalse();
            assertThat(mismatchSpaces).isFalse();
        }
    }

    @Nested
    @DisplayName("마이페이지 리뷰 관련 기능 테스트")
    class MyPageReviews {

        @Test
        @DisplayName("내 리뷰 목록 페이징 조회 - 성공")
        void findByUser_UserIdOrderByCreatedAtDesc_success() throws Exception {
            // given
            User u = saveUser("a@a.com", "a");
            Festival f1 = saveFestivalWithContentId("1111", "축제1");
            Festival f2 = saveFestivalWithContentId("2222", "축제2");

            UserReview r1 = saveReview(u, f1, "첫번째");
            Thread.sleep(5); // createdAt 차이를 위해 살짝 대기
            UserReview r2 = saveReview(u, f2, "두번째");

            var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
            // when
            var page = userReviewRepository.findByUser_UserIdOrderByCreatedAtDesc(u.getUserId(), pageable);

            // then
            assertThat(page.getTotalElements()).isEqualTo(2);
            // 최신순: r2가 먼저
            assertThat(page.getContent().get(0).getUserReviewId()).isEqualTo(r2.getUserReviewId());
            assertThat(page.getContent().get(1).getUserReviewId()).isEqualTo(r1.getUserReviewId());
        }

        @Test
        @DisplayName("내 리뷰 목록 페이징 조회 - 결과 없음")
        void findByUser_empty() {
            var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
            var page = userReviewRepository.findByUser_UserIdOrderByCreatedAtDesc(999L, pageable);
            assertThat(page.getTotalElements()).isZero();
            assertThat(page.getContent()).isEmpty();
        }

        @Test
        @DisplayName("소유자+리뷰ID로 단건 조회 - 성공/실패")
        void findByUser_UserIdAndUserReviewId() {
            User u = saveUser("b@b.com", "b");
            Festival f = saveFestivalWithContentId("3333", "축제3");
            UserReview r = saveReview(u, f, "내용");

            assertThat(userReviewRepository.findByUser_UserIdAndUserReviewId(u.getUserId(), r.getUserReviewId())).isPresent();
            assertThat(userReviewRepository.findByUser_UserIdAndUserReviewId(u.getUserId(), 9999L)).isNotPresent();
            assertThat(userReviewRepository.findByUser_UserIdAndUserReviewId(8888L, r.getUserReviewId())).isNotPresent();
        }

        @Test
        @DisplayName("existsByUser_UserIdAndUserReviewId - 존재/비존재")
        void existsByUser_UserIdAndUserReviewId() {
            User u = saveUser("c@c.com", "c");
            Festival f = saveFestivalWithContentId("4444", "축제4");
            UserReview r = saveReview(u, f, "내용");

            assertThat(userReviewRepository.existsByUser_UserIdAndUserReviewId(u.getUserId(), r.getUserReviewId())).isTrue();
            assertThat(userReviewRepository.existsByUser_UserIdAndUserReviewId(u.getUserId(), 999L)).isFalse();
            assertThat(userReviewRepository.existsByUser_UserIdAndUserReviewId(0L, r.getUserReviewId())).isFalse();
        }
    }

    private User saveUser(String email, String nickname) {
        User u = User.builder()
            .email(email)
            .password("pw")
            .nickname(nickname)
            .signupCompleted(true)
            .build();
        return userRepository.save(u);
    }

    private Festival saveFestivalWithContentId(String contentId, String title) {
        FestivalBasicInfo basic = FestivalBasicInfo.builder()
            .title(title)
            .eventstartdate(LocalDate.now().minusDays(1))
            .eventenddate(LocalDate.now().plusDays(3))
            .mapx(127.12)
            .mapy(37.55)
            .build();

        Festival f = Festival.builder()
            .contentId(contentId)
            .basicInfo(basic)
            .build();
        return festivalRepository.save(f);
    }

    private UserReview saveReview(User u, Festival f, String content) {
        UserReview r = UserReview.builder()
            .user(u)
            .festival(f)
            .rating(0)
            .content(content)
            .build();
        return userReviewRepository.save(r);
    }
}
