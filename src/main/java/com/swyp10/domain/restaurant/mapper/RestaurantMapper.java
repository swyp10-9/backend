package com.swyp10.domain.restaurant.mapper;

import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailInfo2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailIntro2RestaurantDto;
import com.swyp10.domain.restaurant.entity.Restaurant;
import com.swyp10.domain.restaurant.entity.RestaurantBasicInfo;
import com.swyp10.domain.restaurant.entity.RestaurantDetailInfo;
import com.swyp10.domain.restaurant.entity.RestaurantMenu;

import java.util.List;

public class RestaurantMapper {

    public static Restaurant toEntity(AreaBasedList2RestaurantDto searchDto,
                                      DetailIntro2RestaurantDto introDto,
                                      List<DetailInfo2RestaurantDto> detailInfoMenuDtoList) {

        RestaurantBasicInfo basicInfo = toBasicInfo(searchDto);
        RestaurantDetailInfo detailInfo = toDetailInfo(introDto);

        Restaurant restaurant = Restaurant.builder()
            .contentId(searchDto.getContentId())
            .basicInfo(basicInfo)
            .detailInfo(detailInfo)
            .build();

        if (detailInfoMenuDtoList != null) {
            for (DetailInfo2RestaurantDto menuDto : detailInfoMenuDtoList) {
                restaurant.addMenu(toMenu(menuDto));
            }
        }

        return restaurant;
    }

    public static RestaurantBasicInfo toBasicInfo(AreaBasedList2RestaurantDto dto) {
        return RestaurantBasicInfo.builder()
            .addr1(dto.getAddr1())
            .addr2(dto.getAddr2())
            .areacode(dto.getAreacode())
            .createdtime(dto.getCreatedtime())
            .eventstartdate(dto.getEventstartdate())
            .eventenddate(dto.getEventenddate())
            .firstimage(dto.getFirstimage())
            .firstimage2(dto.getFirstimage2())
            .mapx(dto.getMapx())
            .mapy(dto.getMapy())
            .modifiedtime(dto.getModifiedtime())
            .sigungucode(dto.getSigungucode())
            .tel(dto.getTel())
            .title(dto.getTitle())
            .ldongRegnCd(dto.getLDongRegnCd())
            .ldongSignguCd(dto.getLDongSignguCd())
            .build();
    }

    public static RestaurantDetailInfo toDetailInfo(DetailIntro2RestaurantDto dto) {
        return RestaurantDetailInfo.builder()
            .chkcreditcardfood(dto.getChkcreditcardfood())
            .discountinfofood(dto.getDiscountinfofood())
            .firstmenu(dto.getFirstmenu())
            .infocenterfood(dto.getInfocenterfood())
            .kidsfacility(dto.getKidsfacility())
            .opendatefood(dto.getOpendatefood())
            .opentimefood(dto.getOpentimefood())
            .packing(dto.getPacking())
            .parkingfood(dto.getParkingfood())
            .reservationfood(dto.getReservationfood())
            .restdatefood(dto.getRestdatefood())
            .scalefood(dto.getScalefood())
            .seat(dto.getSeat())
            .smoking(dto.getSmoking())
            .treatmenu(dto.getTreatmenu())
            .lcnsno(dto.getLcnsno())
            .build();
    }

    public static RestaurantMenu toMenu(DetailInfo2RestaurantDto dto) {
        return RestaurantMenu.builder()
            .serialnum(dto.getSerialnum())
            .menuname(dto.getMenuname())
            .menuprice(dto.getMenuprice())
            .build();
    }
}
