package com.swyp10.domain.festival.dto.tourapi;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SearchFestival2Dto {
    private String lclsSystm3;
    private String tel;
    private String title;
    private String addr1;
    private String addr2;
    private String areacode;
    private String cat1;
    private String cat2;
    private String cat3;
    private String contentid;
    private String contenttypeid;
    private String createdtime;
    private String eventstartdate;
    private String cpyrhtDivCd;
    private String eventenddate;
    private String firstimage;
    private String firstimage2;
    private String mapx;
    private String mapy;
    private String mlevel;
    private String modifiedtime;
    private String sigungucode;
    private String zipcode;
    private String progresstype;
    private String festivaltype;
    private String lDongRegnCd;
    private String lDongSignguCd;
    private String lclsSystm1;
    private String lclsSystm2;
}
