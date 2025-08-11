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

        System.out.println("=== 북마크 저장 시작 ===");
        System.out.println("userId: " + userId + ", festivalId: " + festivalId);

        // festival_id(PK)로 축제 찾기
        Festival festival = festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND, "존재하지 않는 축제입니다. id=" + festivalId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 사용자입니다. id=" + userId));

        System.out.println("찾은 festival: " + festival.getFestivalId() + ", user: " + user.getUserId());

        // 기존 북마크 유무 확인 (festival_id로 검색)
        Optional<UserBookmark> existingOpt = bookmarkRepository.findByUser_UserIdAndFestival_FestivalId(userId, festivalId);

        if (existingOpt.isPresent()) {
            UserBookmark existing = existingOpt.get();
            System.out.println("기존 북마크 존재, deletedAt: " + existing.getDeletedAt());
            if (existing.getDeletedAt() == null) {
                throw new ApplicationException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
            }
            // soft-delete 상태면 복구
            reviveBookmark(existing);
            System.out.println("북마크 복구 완료: " + existing.getBookmarkId());
            return existing.getBookmarkId();
        }

        // 신규 생성
        UserBookmark created = UserBookmark.builder()
            .festival(festival)
            .user(user)
            .build();
        UserBookmark saved = bookmarkRepository.save(created);
        System.out.println("신규 북마크 생성 완료: " + saved.getBookmarkId());
        return saved.getBookmarkId();
    }

    @Transactional
    protected void reviveBookmark(UserBookmark bookmark) {
        bookmark.revive(); // UserBookmark 엔티티에 revive() 메서드가 있는지 확인 필요
    }
}
