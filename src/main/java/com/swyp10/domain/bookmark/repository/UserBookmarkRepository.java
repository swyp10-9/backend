package com.swyp10.domain.bookmark.repository;

import com.swyp10.domain.bookmark.entity.UserBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long>, UserBookmarkCustomRepository {
    // festival_id로 변경
    Optional<UserBookmark> findByUser_UserIdAndFestival_FestivalId(Long userId, Long festivalId);

    // 유저+축제에 대한 현재 활성(soft delete 되지 않은) 북마크 조회
    Optional<UserBookmark> findByUser_UserIdAndFestival_FestivalIdAndDeletedAtIsNull(Long userId, Long festivalId);

    boolean existsByUser_UserIdAndFestival_FestivalIdAndDeletedAtIsNull(Long userId, Long festivalId);

    Page<UserBookmark> findByUser_UserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    // 필요 시 하드삭제 또는 특정 시점 이전 소프트삭제 데이터 정리용
    long deleteByDeletedAtBefore(LocalDateTime threshold);

    // 특정 유저가 주어진 축제 목록 중 어떤 것들을 북마크했는지 조회
    List<UserBookmark> findByUser_UserIdAndFestival_FestivalIdInAndDeletedAtIsNull(Long userId, Collection<Long> festivalIds);
}
