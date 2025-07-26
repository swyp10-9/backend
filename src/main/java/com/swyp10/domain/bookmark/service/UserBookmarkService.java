package com.swyp10.domain.bookmark.service;

import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBookmarkService {

    private final UserBookmarkRepository userBookmarkRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;

    public UserBookmark getUserBookmark(Long bookmarkId) {
        return userBookmarkRepository.findById(bookmarkId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "UserBookmark not found: " + bookmarkId));
    }

    @Transactional
    public UserBookmark createUserBookmark(UserBookmark userBookmark) {
        return userBookmarkRepository.save(userBookmark);
    }

    @Transactional
    public void deleteUserBookmark(Long bookmarkId) {
        userBookmarkRepository.deleteById(bookmarkId);
    }

    @Transactional
    public Long addBookmark(Long userId, Long festivalId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "사용자를 찾을 수 없습니다."));
        Festival festival = festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "축제를 찾을 수 없습니다."));

        // 이미 북마크 되어있는지 체크
        userBookmarkRepository.findByUser_UserIdAndFestival_FestivalId(userId, festivalId).ifPresent(bookmark -> {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "이미 북마크한 축제입니다.");
        });

        UserBookmark userBookmark = UserBookmark.builder()
            .user(user)
            .festival(festival)
            .createdAt(LocalDateTime.now())
            .build();

        userBookmarkRepository.save(userBookmark);

        return null;
    }
}
