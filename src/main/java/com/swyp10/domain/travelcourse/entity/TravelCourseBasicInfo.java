package com.swyp10.domain.travelcourse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelCourseBasicInfo {

    @Column(columnDefinition = "TEXT")
    private String addr1;

    private String areacode;

    private String createdtime;

    @Column(columnDefinition = "TEXT")
    private String firstimage;

    @Column(columnDefinition = "TEXT")
    private String firstimage2;

    private String mapx;

    private String mapy;

    private String modifiedtime;

    private String sigungucode;

    private String tel;

    private String title;

    private String lDongRegnCd;

    private String lDongSignguCd;
}
