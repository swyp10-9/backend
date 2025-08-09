package com.swyp10.domain.bookmark.repository;

import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Set;

public interface UserBookmarkCustomRepository {
    /**
     * userId가 주어진 festivalIds 중 북마크한 festivalId 집합 반환
     */
    Set<Long> findBookmarkedFestivalIds(Long userId, Collection<Long> festivalIds);

    Page<FestivalSummaryResponse> findBookmarkedFestivals(Long userId, Pageable pageable);
}
