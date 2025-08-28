package com.swyp10.domain.festival.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "festival_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalStatistics {

    @Id
    @Column(name = "festival_id")
    private Long festivalId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "bookmark_count", nullable = false)
    private int bookmarkCount;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    private BigDecimal ratingAvg;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static FestivalStatistics createEmpty(Festival festival) {
        return FestivalStatistics.builder()
            .festival(festival)
            .viewCount(0)
            .bookmarkCount(0)
            .ratingAvg(BigDecimal.ZERO)
            .ratingCount(0)
            .build();
    }

    @Builder
    public FestivalStatistics(Festival festival, int viewCount, int bookmarkCount, BigDecimal ratingAvg, int ratingCount) {
        this.festival = festival;
        this.viewCount = viewCount;
        this.bookmarkCount = bookmarkCount;
        this.ratingAvg = ratingAvg;
        this.ratingCount = ratingCount;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }
}
