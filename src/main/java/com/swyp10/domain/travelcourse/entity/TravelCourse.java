package com.swyp10.domain.travelcourse.entity;

import com.swyp10.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travel_courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelCourse extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    @Builder.Default
    private TravelDifficulty difficultyLevel = TravelDifficulty.NORMAL;

    @Column(name = "content_id", unique = true, nullable = false, length = 32)
    private String contentId;

    @Embedded
    private TravelCourseBasicInfo basicInfo;

    @OneToMany(mappedBy = "travelCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TravelCourseDetailInfo> detailInfos = new ArrayList<>();

    public void updateBasicInfo(TravelCourseBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    // 연관관계 메서드
    public void addDetailInfo(TravelCourseDetailInfo detailInfo) {
        detailInfos.add(detailInfo);
        detailInfo.setTravelCourse(this);
    }

    public void clearDetailInfos() {
        for (TravelCourseDetailInfo info : detailInfos) {
            info.setTravelCourse(null);
        }
        detailInfos.clear();
    }
}
