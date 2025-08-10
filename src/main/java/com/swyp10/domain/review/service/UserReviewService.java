package com.swyp10.domain.review.service;

import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.domain.review.dto.request.FestivalReviewCreateRequest;
import com.swyp10.domain.review.dto.response.FestivalReviewListResponse;
import com.swyp10.domain.review.dto.response.FestivalReviewResponse;
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

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReviewService {

    private final UserReviewRepository userReviewRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;

    public UserReview getUserReview(Long userReviewId) {
        return userReviewRepository.findById(userReviewId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "UserReview not found: " + userReviewId));
    }

    /**
     * 축제 리뷰 목록 (페이징)
     */
    public FestivalReviewListResponse getFestivalReviews(Long festivalId, PageRequest pageRequest) {
        // festival_id(PK)로 축제 찾기
        Festival festival = festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND, "존재하지 않는 축제입니다. id=" + festivalId));

        Pageable pageable = toPageable(pageRequest, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 리뷰는 content_id로 저장되어 있으므로 content_id로 검색
        var page = userReviewRepository.findByFestival_ContentIdOrderByCreatedAtDesc(festival.getContentId(), pageable);

        return FestivalReviewListResponse.builder()
            .totalElements(page.getTotalElements())
            .content(page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * 리뷰 등록
     */
    @Transactional
    public Long createFestivalReview(Long userId, Long festivalId, FestivalReviewCreateRequest request) {
        if (userId == null) {
            throw new ApplicationException(ErrorCode.USER_NOT_FOUND, "로그인이 필요합니다.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 사용자입니다. id=" + userId));

        Festival festival = festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND, "존재하지 않는 축제입니다. id=" + festivalId));

        // (옵션) 동일 유저의 중복 리뷰 방지
        // if (userReviewRepository.existsByUser_UserIdAndFestival_FestivalId(userId, festivalId)) {
        //     throw new ApplicationException(ErrorCode.REVIEW_ALREADY_EXISTS, "이미 리뷰를 작성했습니다.");
        // }

        UserReview review = UserReview.builder()
            .user(user)
            .festival(festival)
            .rating(0) // 별점 필드 쓰지 않으면 0으로
            .content(request.getContent())
            .build();

        return userReviewRepository.save(review).getUserReviewId();
    }

    // ====== private helpers ======

    private Pageable toPageable(PageRequest req, Sort defaultSort) {
        Sort sort = defaultSort;
        return org.springframework.data.domain.PageRequest.of(req.getPage(), req.getSize(), sort);
    }

    private FestivalReviewResponse toDto(UserReview r) {
        return FestivalReviewResponse.builder()
            .id(r.getUserReviewId())
            .nickname(r.getUser() != null ? r.getUser().getNickname() : null)
            .profileImage(r.getUser() != null ? r.getUser().getProfileImage() : null)
            .content(r.getContent())
            .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate() : null)
            .build();
    }

}
