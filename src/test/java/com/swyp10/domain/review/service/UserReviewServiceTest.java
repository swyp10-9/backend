package com.swyp10.domain.review.service;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.domain.review.dto.request.FestivalReviewCreateRequest;
import com.swyp10.domain.review.dto.response.FestivalReviewListResponse;
import com.swyp10.domain.review.entity.UserReview;
import com.swyp10.domain.review.repository.UserReviewRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.global.page.PageRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("UserReviewService 통합 테스트")
class UserReviewServiceIT {

    @Autowired UserReviewService userReviewService;
    @Autowired UserRepository userRepository;
    @Autowired FestivalRepository festivalRepository;
    @Autowired UserReviewRepository userReviewRepository;

    // =========================
    // 성공 케이스
    // =========================

    @Test
    @DisplayName("getFestivalReviews - 축제 존재 & 리뷰 페이징 조회 성공")
    void getFestivalReviews_success() {
        // given
        Festival festival = saveFestivalWithContentId("100", "부산 불꽃축제");
        User user1 = saveUser("u1@test.com", "유저1");
        User user2 = saveUser("u2@test.com", "유저2");
        saveReview(user1, festival, "최고의 축제!");
        saveReview(user2, festival, "불꽃이 예뻤어요!");

        PageRequest pageReq = PageRequest.builder()
            .page(0)
            .size(10)
            .build();

        // when
        FestivalReviewListResponse res = userReviewService.getFestivalReviews(100L, pageReq);

        // then
        assertThat(res.getTotalElements()).isEqualTo(2);
        assertThat(res.getContent()).hasSize(2);
        // 최신순 정렬 확인(서비스에서 createdAt DESC)
        List<String> contents = res.getContent().stream().map(r -> r.getContent()).toList();
        assertThat(contents).containsExactlyInAnyOrder("최고의 축제!", "불꽃이 예뻤어요!");
    }

    @Test
    @DisplayName("createFestivalReview - 로그인 사용자 & 축제 존재 시 리뷰 등록 성공")
    void createFestivalReview_success() {
        // given
        User user = saveUser("login@test.com", "로그인유저");
        Festival festival = saveFestivalOnlyId("서울 야간 축제");

        FestivalReviewCreateRequest req = new FestivalReviewCreateRequest();
        req.setContent("야경이 끝내줍니다!");

        // when
        Long reviewId = userReviewService.createFestivalReview(user.getUserId(), festival.getFestivalId(), req);

        // then
        assertThat(reviewId).isNotNull();
        UserReview saved = userReviewRepository.findById(reviewId).orElseThrow();
        assertThat(saved.getUser().getUserId()).isEqualTo(user.getUserId());
        assertThat(saved.getFestival().getFestivalId()).isEqualTo(festival.getFestivalId());
        assertThat(saved.getContent()).isEqualTo("야경이 끝내줍니다!");
    }

    // =========================
    // 실패 케이스
    // =========================

    @Test
    @DisplayName("getFestivalReviews - 축제(contentId) 없음 -> FESTIVAL_NOT_FOUND")
    void getFestivalReviews_festivalNotFound() {
        // given
        PageRequest pageReq = PageRequest.builder().page(0).size(10).build();

        // when / then
        assertThatThrownBy(() -> userReviewService.getFestivalReviews(999999L, pageReq))
            .isInstanceOf(ApplicationException.class)
            .satisfies(ex -> {
                ApplicationException ae = (ApplicationException) ex;
                assertThat(ae.getErrorCode()).isEqualTo(ErrorCode.FESTIVAL_NOT_FOUND);
            });
    }

    @Test
    @DisplayName("createFestivalReview - userId null -> USER_NOT_FOUND")
    void createFestivalReview_userIdNull() {
        // given
        Festival festival = saveFestivalOnlyId("서울 야간 축제");
        FestivalReviewCreateRequest req = new FestivalReviewCreateRequest();
        req.setContent("비로그인 작성 불가");

        // when / then
        assertThatThrownBy(() -> userReviewService.createFestivalReview(null, festival.getFestivalId(), req))
            .isInstanceOf(ApplicationException.class)
            .satisfies(ex -> {
                ApplicationException ae = (ApplicationException) ex;
                assertThat(ae.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            });
    }

    @Test
    @DisplayName("createFestivalReview - 사용자 없음 -> USER_NOT_FOUND")
    void createFestivalReview_userNotFound() {
        // given
        Festival festival = saveFestivalOnlyId("서울 야간 축제");
        FestivalReviewCreateRequest req = new FestivalReviewCreateRequest();
        req.setContent("존재하지 않는 사용자");

        // when / then
        assertThatThrownBy(() -> userReviewService.createFestivalReview(123456789L, festival.getFestivalId(), req))
            .isInstanceOf(ApplicationException.class)
            .satisfies(ex -> {
                ApplicationException ae = (ApplicationException) ex;
                assertThat(ae.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            });
    }

    @Test
    @DisplayName("createFestivalReview - 축제(id) 없음 -> FESTIVAL_NOT_FOUND")
    void createFestivalReview_festivalNotFound() {
        // given
        User user = saveUser("user@test.com", "유저");
        FestivalReviewCreateRequest req = new FestivalReviewCreateRequest();
        req.setContent("없는 축제에 리뷰");

        // when / then
        assertThatThrownBy(() -> userReviewService.createFestivalReview(user.getUserId(), 999999L, req))
            .isInstanceOf(ApplicationException.class)
            .satisfies(ex -> {
                ApplicationException ae = (ApplicationException) ex;
                assertThat(ae.getErrorCode()).isEqualTo(ErrorCode.FESTIVAL_NOT_FOUND);
            });
    }

    // =========================
    // 엣지 케이스
    // =========================

    @Test
    @DisplayName("getFestivalReviews - 리뷰 0건인 축제면 빈 리스트 반환")
    void getFestivalReviews_empty() {
        // given: 축제는 있으나 리뷰 없음
        saveFestivalWithContentId("2000", "리뷰 없음 축제");
        PageRequest pageReq = PageRequest.builder().page(0).size(10).build();

        // when
        FestivalReviewListResponse res = userReviewService.getFestivalReviews(2000L, pageReq);

        // then
        assertThat(res.getTotalElements()).isZero();
        assertThat(res.getContent()).isEmpty();
    }

    @Test
    @DisplayName("createFestivalReview - 내용이 비어있어도(서비스 레벨) 저장됨 (컨트롤러 검증 우회 엣지)")
    void createFestivalReview_emptyContent_edge() {
        // given (컨트롤러의 @Valid 검증은 통합 테스트 범위 밖)
        User user = saveUser("edge@test.com", "엣지");
        Festival festival = saveFestivalOnlyId("빈 내용 축제");

        FestivalReviewCreateRequest req = new FestivalReviewCreateRequest();
        req.setContent(""); // 빈 문자열

        // when
        Long id = userReviewService.createFestivalReview(user.getUserId(), festival.getFestivalId(), req);

        // then
        assertThat(id).isNotNull();
        UserReview r = userReviewRepository.findById(id).orElseThrow();
        assertThat(r.getContent()).isEqualTo(""); // 서비스 레벨에서는 저장됨
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
            .eventenddate(LocalDate.now().plusDays(1))
            .mapx(127.1)
            .mapy(37.5)
            .build();

        Festival f = Festival.builder()
            .contentId(contentId)   // ★ getFestivalReviews는 contentId 기반 조회
            .basicInfo(basic)
            .build();

        return festivalRepository.save(f);
    }

    private Festival saveFestivalOnlyId(String title) {
        // create + return generated festivalId (Long)
        FestivalBasicInfo basic = FestivalBasicInfo.builder()
            .title(title)
            .eventstartdate(LocalDate.now())
            .eventenddate(LocalDate.now().plusDays(2))
            .mapx(127.2)
            .mapy(37.6)
            .build();

        Festival f = Festival.builder()
            .contentId("AUTO-" + System.nanoTime())
            .basicInfo(basic)
            .build();
        return festivalRepository.save(f);
    }

    private void saveReview(User u, Festival f, String content) {
        UserReview r = UserReview.builder()
            .user(u)
            .festival(f)
            .rating(0)
            .content(content)
            .build();
        userReviewRepository.save(r);
    }
}
