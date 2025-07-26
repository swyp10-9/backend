package com.swyp10.domain.bookmark.repository;

import com.swyp10.domain.bookmark.entity.UserBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {
    Optional<UserBookmark> findByUserIdAndFestivalId(Long userId, Long festivalId);
}
