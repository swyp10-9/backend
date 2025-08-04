package com.swyp10.domain.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalBasicInfo {
    private String addr1;

    private String areacode;

    private String areacodeName;

    private String contenttypeid;

    private String createdtime;

    private String eventstartdate;

    private String eventenddate;

    @Column(columnDefinition = "TEXT")
    private String firstimage;

    @Column(columnDefinition = "TEXT")
    private String firstimage2;

    private String mapx;

    private String mapy;

    private String modifiedtime;

    private String sigungucode;

    @Column(columnDefinition = "TEXT")
    private String tel;

    private String title;

    private String lDongRegnCd;

    private String lDongSignguCd;

    private String lDongRegnName;

    private String lDongSignguName;

    private String lclsSystm1;

    private String progresstype;

    private String festivaltype;
}
