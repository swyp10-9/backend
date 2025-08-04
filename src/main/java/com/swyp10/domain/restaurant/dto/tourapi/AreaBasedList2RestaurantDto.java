package com.swyp10.domain.restaurant.dto.tourapi;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaBasedList2RestaurantDto {
    private String contentId;
    private String contentTypeId;
    private String addr1;
    private String addr2;
    private String areacode;
    private String createdtime;
    private String eventstartdate;
    private String eventenddate;
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

