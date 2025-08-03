package com.swyp10.domain.festival.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swyp10.common.BaseTimeEntity;
import com.swyp10.domain.festival.enums.FestivalPersonalityType;
import com.swyp10.domain.festival.enums.FestivalStatus;
import com.swyp10.domain.festival.enums.FestivalTheme;
import com.swyp10.domain.festival.enums.FestivalWithWhom;
import com.swyp10.domain.region.entity.Region;
import com.swyp10.domain.review.entity.UserReview;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "festivals")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Festival extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "content_id", unique = true, nullable = false, length = 32)
    private String contentId;

    @Embedded
    private FestivalBasicInfo basicInfo;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Embedded
    private FestivalDetailIntro detailIntro;

    @Enumerated(EnumType.STRING)
    private FestivalPersonalityType personalityType;

    @Enumerated(EnumType.STRING)
    private FestivalStatus status;

    @Enumerated(EnumType.STRING)
    private FestivalTheme theme;

    @Enumerated(EnumType.STRING)
    private FestivalWithWhom withWhom;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FestivalImage> detailImages = new ArrayList<>();

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

    // 
    public void updateOverview(String overview) {
        this.overview = overview;
    }

    public void updateDetailIntro(FestivalDetailIntro detailIntro) {
        this.detailIntro = detailIntro;
    }

    public void updateBasicInfo(FestivalBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    // 연관 관계 메서드
    public void initializeStatistics() {
        FestivalStatistics stats = FestivalStatistics.createEmpty(this);
        this.statistics = stats;
    }

    public void addDetailImage(FestivalImage image) {
        detailImages.add(image);
        image.setFestival(this);
    }

    public void clearDetailImages() {
        detailImages.forEach(img -> img.setFestival(null));
        detailImages.clear();
    }
}
