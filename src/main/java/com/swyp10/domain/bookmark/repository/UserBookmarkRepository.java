package com.swyp10.domain.bookmark.repository;

import com.swyp10.domain.bookmark.entity.UserBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {
}
