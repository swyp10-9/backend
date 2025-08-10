package com.swyp10.domain.mypage.service;

import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.mypage.dto.request.MyInfoUpdateRequest;
import com.swyp10.domain.mypage.dto.response.MyInfoResponse;
import com.swyp10.domain.mypage.dto.response.MyReviewListResponse;
import com.swyp10.domain.mypage.dto.response.MyReviewResponse;
import com.swyp10.domain.review.entity.UserReview;
import com.swyp10.domain.review.repository.UserReviewRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.global.page.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserReviewRepository userReviewRepository;
    private final UserRepository userRepository;
    private final UserBookmarkRepository userBookmarkRepository;

    /**
     * 북마크 취소(Soft Delete)
     */
    @Transactional
    public void cancelBookmark(Long userId, Long festivalId) {
        if (userId == null) {
            throw new ApplicationException(ErrorCode.USER_NOT_FOUND, "로그인이 필요합니다.");
        }
        UserBookmark bookmark = userBookmarkRepository
            .findByUser_UserIdAndFestival_ContentIdAndDeletedAtIsNull(userId, String.valueOf(festivalId))
            .orElseThrow(() -> new ApplicationException(ErrorCode.BOOKMARK_NOT_FOUND, "활성 상태의 북마크가 없습니다."));

        // soft delete
        bookmark.markDeleted();
    }

    /**
     * 내 정보 변경 (닉네임)
     */
    @Transactional
    public MyInfoResponse updateMyInfo(Long userId, MyInfoUpdateRequest request) {
        if (userId == null) {
            throw new ApplicationException(ErrorCode.USER_NOT_FOUND, "로그인이 필요합니다.");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 사용자입니다. id=" + userId));

        user.updateProfile(request.getNickname());

        return MyInfoResponse.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .profileImage(user.getProfileImage())
            .build();
    }

    /**
     * 내 리뷰 목록 조회 (페이징)
     */
    public MyReviewListResponse getMyReviews(Long userId, PageRequest pageRequest) {
        if (userId == null) {
            throw new ApplicationException(ErrorCode.USER_NOT_FOUND, "로그인이 필요합니다.");
        }

        // 기본 정렬: 작성일 내림차순
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
            pageRequest.getPage(),
            pageRequest.getSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        var page = userReviewRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);

        List<MyReviewResponse> items = page.getContent().stream()
            .map(this::toMyReviewResponse)
            .toList();

        return MyReviewListResponse.builder()
            .totalElements(page.getTotalElements())
            .content(items)
            .build();
    }

    /**
     * 내 리뷰 삭제 (소유자 검증 + 물리 삭제)
     */
    @Transactional
    public void deleteMyReview(Long userId, Long reviewId) {
        if (userId == null) {
            throw new ApplicationException(ErrorCode.USER_NOT_FOUND, "로그인이 필요합니다.");
        }

        UserReview review = userReviewRepository.findByUser_UserIdAndUserReviewId(userId, reviewId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.REVIEW_NOT_FOUND, "리뷰를 찾을 수 없습니다. id=" + reviewId));

        userReviewRepository.delete(review);
    }



    // ====== mapper ======
    private MyReviewResponse toMyReviewResponse(UserReview r) {
        Festival f = r.getFestival();

        Long festivalId = (f != null) ? f.getFestivalId() : null;
        String festivalTitle = (f != null && f.getBasicInfo() != null) ? f.getBasicInfo().getTitle() : null;
        String festivalThumbnail = (f != null && f.getBasicInfo() != null) ? f.getBasicInfo().getFirstimage2() : null;
        LocalDate createdDate = (r.getCreatedAt() != null) ? r.getCreatedAt().toLocalDate() : null;

        return MyReviewResponse.builder()
            .id(r.getUserReviewId())
            .festivalId(festivalId)
            .festivalTitle(festivalTitle)
            .festivalThumbnail(festivalThumbnail)
            .content(r.getContent())
            .createdAt(createdDate)
            .build();
    }
}
