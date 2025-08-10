package com.swyp10.domain.mypage.service;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.domain.mypage.dto.response.MyReviewListResponse;
import com.swyp10.domain.review.entity.UserReview;
import com.swyp10.domain.review.repository.UserReviewRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.global.page.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("MyPageService 통합 테스트")
class MyPageServiceTest {

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private UserReviewRepository userReviewRepository;

    // ====== 유틸 ======
    private User saveUser(String email, String nick) {
        return userRepository.save(
            User.builder()
                .email(email).password("pw").nickname(nick)
                .signupCompleted(true)
                .build()
        );
    }

    private Festival saveFestival(String contentId, String title) {
        FestivalBasicInfo bi = FestivalBasicInfo.builder()
            .title(title)
            .firstimage2("https://img")
            .build();
        return festivalRepository.save(
            Festival.builder()
                .contentId(contentId)
                .basicInfo(bi)
                .build()
        );
    }

    private UserReview saveReview(User u, Festival f, String content) {
        return userReviewRepository.save(
            UserReview.builder()
                .user(u)
                .festival(f)
                .rating(0)
                .content(content)
                .build()
        );
    }

    // ====== getMyReviews ======

    @Test
    @DisplayName("내 리뷰 목록 조회 - 성공 (페이징 + 매핑 필드 확인)")
    void getMyReviews_success() throws Exception {
        // given
        User me = saveUser("me@me.com", "me");
        Festival f1 = saveFestival("1111", "축제1");
        Festival f2 = saveFestival("2222", "축제2");

        saveReview(me, f1, "재밌었음");
        Thread.sleep(3);
        saveReview(me, f2, "최고!");

        PageRequest req = PageRequest.builder().page(0).size(10).build();

        // when
        MyReviewListResponse res = myPageService.getMyReviews(me.getUserId(), req);

        // then
        assertThat(res.getTotalElements()).isEqualTo(2);
        assertThat(res.getContent()).hasSize(2);

        var first = res.getContent().get(0);
        assertThat(first.getFestivalTitle()).isNotBlank();
        assertThat(first.getFestivalThumbnail()).isNotBlank();
        assertThat(first.getContent()).isNotBlank();
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 - 실패 (userId == null)")
    void getMyReviews_unauthenticated_fail() {
        PageRequest req = PageRequest.builder().page(0).size(10).build();
        assertThatThrownBy(() -> myPageService.getMyReviews(null, req))
            .isInstanceOf(ApplicationException.class)
            .satisfies(ex ->
                assertThat(((ApplicationException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND)
            );
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 - 결과 없음(리뷰를 작성하지 않은 유저)")
    void getMyReviews_empty_success() {
        User me = saveUser("noreview@user.com", "none");
        PageRequest req = PageRequest.builder().page(0).size(10).build();

        MyReviewListResponse res = myPageService.getMyReviews(me.getUserId(), req);

        assertThat(res.getTotalElements()).isZero();
        assertThat(res.getContent()).isEmpty();
    }

    // ====== deleteMyReview ======

    @Test
    @DisplayName("내 리뷰 삭제 - 성공")
    void deleteMyReview_success() {
        User me = saveUser("del@me.com", "me");
        Festival f = saveFestival("1111", "삭제축제");
        UserReview r = saveReview(me, f, "삭제할 리뷰");

        // when
        myPageService.deleteMyReview(me.getUserId(), r.getUserReviewId());

        // then
        assertThat(userReviewRepository.findById(r.getUserReviewId())).isEmpty();
    }

    @Test
    @DisplayName("내 리뷰 삭제 - 실패 (userId == null)")
    void deleteMyReview_unauthenticated_fail() {
        assertThatThrownBy(() -> myPageService.deleteMyReview(null, 1L))
            .isInstanceOf(ApplicationException.class)
            .satisfies(ex ->
                assertThat(((ApplicationException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND)
            );
    }

    @Test
    @DisplayName("내 리뷰 삭제 - 실패 (소유자 아님)")
    void deleteMyReview_notOwner_fail() {
        User owner = saveUser("owner@me.com", "owner");
        User other = saveUser("other@me.com", "other");
        Festival f = saveFestival("2222", "축제");
        UserReview r = saveReview(owner, f, "오너 리뷰");

        assertThatThrownBy(() -> myPageService.deleteMyReview(other.getUserId(), r.getUserReviewId()))
            .isInstanceOf(ApplicationException.class)
            .satisfies(ex ->
                assertThat(((ApplicationException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.REVIEW_NOT_FOUND) // 소유자 검증 실패시 NOT_FOUND 정책
            );
    }

    @Test
    @DisplayName("내 리뷰 삭제 - 실패 (리뷰 없음)")
    void deleteMyReview_notFound_fail() {
        User me = saveUser("x@x.com", "x");
        assertThatThrownBy(() -> myPageService.deleteMyReview(me.getUserId(), 999999L))
            .isInstanceOf(ApplicationException.class)
            .satisfies(ex ->
                assertThat(((ApplicationException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.REVIEW_NOT_FOUND)
            );
    }
}
