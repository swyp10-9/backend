package com.swyp10.domain.festival.repository;

import com.swyp10.domain.festival.entity.Festival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FestivalRepository extends JpaRepository<Festival, Long>, FestivalCustomRepository {
    @EntityGraph(attributePaths = {"basicInfo", "detailIntro", "detailImages"})
    Optional<Festival> findByContentId(String contentId);
}
