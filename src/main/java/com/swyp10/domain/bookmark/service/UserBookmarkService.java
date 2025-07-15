package com.swyp10.domain.bookmark.service;

import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
import com.swyp10.global.exception.ApplicationException;
import com.swyp10.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBookmarkService {

    private final UserBookmarkRepository userBookmarkRepository;

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
}
