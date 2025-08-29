package com.swyp10.domain.festival.repository;

import com.swyp10.domain.festival.entity.Festival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FestivalRepository extends JpaRepository<Festival, Long>, FestivalCustomRepository {
    @EntityGraph(attributePaths = {"basicInfo", "detailIntro", "detailImages"})
    Optional<Festival> findByContentId(String contentId);

    @Modifying
    @Query("UPDATE FestivalStatistics fs SET fs.viewCount = fs.viewCount + 1, fs.updatedAt = CURRENT_TIMESTAMP WHERE fs.festivalId = :festivalId")
    int incrementViewCount(@Param("festivalId") Long festivalId);
}
