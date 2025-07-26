package com.swyp10.domain.search.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_keywords",
    indexes = {
        @Index(name = "idx_search_keyword", columnList = "keyword"),
        @Index(name = "idx_search_count", columnList = "count")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SearchKeyword {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String keyword;

    @Column(nullable = false)
    private Long count;

    @Column(name = "last_searched_at")
    @UpdateTimestamp
    private LocalDateTime lastSearchedAt;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 검색어 검색 시 count 증가 및 lastSearchedAt 갱신
    public void increaseCount() {
        this.count += 1;
        this.lastSearchedAt = LocalDateTime.now();
    }
}
