package com.swyp10.domain.search.repository;

import com.swyp10.domain.search.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {
    Optional<SearchKeyword> findByKeyword(String keyword);

    List<SearchKeyword> findTop10ByOrderByCountDescLastSearchedAtDesc();
}
