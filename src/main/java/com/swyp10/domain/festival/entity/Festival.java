package com.swyp10.domain.festival.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swyp10.domain.region.entity.Region;
import com.swyp10.domain.review.entity.UserReview;
import com.swyp10.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "festivals")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Festival extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "festival_id")
    private Long id;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private FestivalTheme theme;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    @OneToOne(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FestivalStatistics statistics;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code", updatable = false)
    private Region region;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<FestivalTravelCourse> travelCourses = new ArrayList<>();

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<UserReview> reviews = new ArrayList<>();

    // 연관 관계 메서드
    public void initializeStatistics() {
        FestivalStatistics stats = FestivalStatistics.createEmpty(this);
        this.statistics = stats;
    }
}
