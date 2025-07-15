package com.swyp10.domain.recommendation.entity;

import com.swyp10.domain.festival.entity.Festival;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_recommendations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(buildMethodName = "of")
public class MonthlyRecommendation {

    @Id
    @Column(name = "festival_id")
    @Setter(AccessLevel.NONE)
    private Long festivalId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Column(name = "sort_sq")
    private Long sortSq;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
