package com.swyp10.domain.travelcourse.dto.tourapi;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchTravelCourseDto {
    private String addr1;
    private String areacode;
    private String contentid;
    private String contenttypeid;
    private String createdtime;
    private String firstimage;
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
