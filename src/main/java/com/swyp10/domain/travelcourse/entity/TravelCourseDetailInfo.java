package com.swyp10.domain.travelcourse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "travel_course_detail_infos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelCourseDetailInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_info_id")
    private Long detailInfoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_course_id")
    private TravelCourse travelCourse;

    @Column(length = 10)
    private String subnum;

    @Column(length = 50)
    private String subcontentid;

    @Column(length = 255)
    private String subname;

    @Column(columnDefinition = "TEXT")
    private String subdetailoverview;

    @Column(columnDefinition = "TEXT")
    private String subdetailimg;

    public void setTravelCourse(TravelCourse travelCourse) {
        this.travelCourse = travelCourse;
    }
}
