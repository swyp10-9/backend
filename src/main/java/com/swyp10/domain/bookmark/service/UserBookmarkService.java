package com.swyp10.domain.bookmark.service;

import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBookmarkService {

    private final UserBookmarkRepository bookmarkRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;

    /**
     * 북마크 저장 (이미 존재 & 삭제되지 않음 -> 에러 / soft-delete 상태 -> 복구 / 없음 -> 신규생성)
     */
    @Transactional
    public Long addBookmark(Long userId, Long festivalId) {
        if (userId == null) {
            throw new ApplicationException(ErrorCode.USER_NOT_FOUND, "로그인이 필요합니다.");
        }

        Festival festival = festivalRepository.findByContentId(String.valueOf(festivalId))
            .orElseThrow(() -> new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND, "존재하지 않는 축제입니다. id=" + festivalId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 사용자입니다. id=" + userId));

        // 기존 북마크 유무 확인 (soft 포함)
        Optional<UserBookmark> existingOpt = bookmarkRepository.findByUser_UserIdAndFestival_ContentId(userId, String.valueOf(festivalId));

        if (existingOpt.isPresent()) {
            UserBookmark existing = existingOpt.get();
            if (existing.getDeletedAt() == null) {
                throw new ApplicationException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
            }
            // soft-delete 상태면 복구
            reviveBookmark(existing);
            return existing.getBookmarkId();
        }

        // 신규 생성
        UserBookmark created = UserBookmark.builder()
            .festival(festival)
            .user(user)
            .build();
        UserBookmark saved = bookmarkRepository.save(created);
        return saved.getBookmarkId();
    }

    @Transactional
    protected void reviveBookmark(UserBookmark bookmark) {
        // 단순히 deletedAt null 처리(복구)
        // 엔터티에 revive 메서드가 없다면 setter 또는 직접 null 세팅
        try {
            var field = UserBookmark.class.getDeclaredField("deletedAt");
            field.setAccessible(true);
            field.set(bookmark, null);
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, "북마크 복구 중 오류가 발생했습니다.");
        }
    }

    /**
     * 마이페이지 - 내 북마크 목록 (간단 DTO) 페이지 조회
     */
    public MyBookmarkPageResponse getMyBookmarks(Long userId, int page, int size) {
        if (userId == null) {
            throw new ApplicationException(ErrorCode.USER_NOT_FOUND, "로그인이 필요합니다.");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserBookmark> pageEntity = bookmarkRepository.findByUser_UserIdAndDeletedAtIsNull(userId, pageable);

        List<MyBookmarkItem> items = pageEntity.getContent().stream()
            .map(this::toBookmarkItem)
            .toList();

        return new MyBookmarkPageResponse(
            items,
            pageEntity.getNumber(),
            pageEntity.getSize(),
            pageEntity.getTotalElements(),
            pageEntity.getTotalPages(),
            pageEntity.isFirst(),
            pageEntity.isLast(),
            pageEntity.isEmpty()
        );
    }

    private MyBookmarkItem toBookmarkItem(UserBookmark ub) {
        Festival f = ub.getFestival();
        return new MyBookmarkItem(
            ub.getBookmarkId(),
            f.getFestivalId(),
            f.getBasicInfo() != null ? f.getBasicInfo().getTitle() : null,
            f.getBasicInfo() != null ? f.getBasicInfo().getFirstimage2() : null,
            ub.getCreatedAt()
        );
    }

    /**
     * 축제 리스트 응답에 'bookmarked' 플래그 셋팅.
     * - 유저가 없으면 그대로 리턴
     * - 유저가 있으면 IN 조회로 최적화
     */
    public void markBookmarkedFlags(Long userId, List<FestivalSummaryResponse> summaries) {
        if (userId == null || summaries == null || summaries.isEmpty()) return;

        List<Long> ids = summaries.stream()
            .map(FestivalSummaryResponse::getId)
            .filter(Objects::nonNull)
            .toList();

        if (ids.isEmpty()) return;

        Set<Long> bookmarkedIds = bookmarkRepository.findBookmarkedFestivalIds(userId, ids);

        summaries.forEach(s -> {
            boolean isBookmarked = s.getId() != null && bookmarkedIds.contains(s.getId());
            setBookmarkedField(s, isBookmarked);
        });
    }

    private void setBookmarkedField(FestivalSummaryResponse s, boolean value) {
        try {
            var f = FestivalSummaryResponse.class.getDeclaredField("bookmarked");
            f.setAccessible(true);
            f.set(s, value);
        } catch (Exception ignore) {
        }
    }

    @Transactional
    public void cancelBookmark(Long userId, Long festivalId) {
        UserBookmark ub = bookmarkRepository.findByUser_UserIdAndFestival_ContentId(userId, String.valueOf(festivalId))
            .orElseThrow(() -> new ApplicationException(ErrorCode.BOOKMARK_NOT_FOUND));
        if (ub.getDeletedAt() != null) return; // 이미 취소됨
        ub.markDeleted();
    }

    // ====== 마이페이지 응답 DTO(간단) ======
    public record MyBookmarkPageResponse(
        List<MyBookmarkItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
    ) {}

    public record MyBookmarkItem(
        Long bookmarkId,
        Long festivalId,
        String festivalTitle,
        String festivalThumbnail,
        LocalDateTime bookmarkedAt
    ) {}
}
