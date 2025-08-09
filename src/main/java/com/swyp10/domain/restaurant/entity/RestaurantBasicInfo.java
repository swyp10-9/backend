package com.swyp10.domain.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class RestaurantBasicInfo {

    private String addr1;

    private String addr2;

    private String areacode;

    private String createdtime;

    private String eventstartdate;

    private String eventenddate;

    @Column(columnDefinition = "TEXT")
    private String firstimage;

    @Column(columnDefinition = "TEXT")
    private String firstimage2;

    private Double mapx;

    private Double mapy;

    private String sigungucode;

    private String modifiedtime;

    private String tel;

    private String title;

    private String ldongRegnCd;

    private String ldongSignguCd;
}
