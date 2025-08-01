package com.swyp10.domain.festival.mapper;

import com.swyp10.domain.festival.dto.tourapi.*;
import com.swyp10.domain.festival.entity.*;

import java.util.List;
import java.util.stream.Collectors;

public class FestivalMapper {

    public static FestivalBasicInfo toBasicInfo(SearchFestival2Dto dto) {
        return FestivalBasicInfo.builder()
            .addr1(dto.getAddr1())
            .areacode(dto.getAreacode())
            .contenttypeid(dto.getContenttypeid())
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
            .lDongRegnCd(dto.getLDongRegnCd())
            .lDongSignguCd(dto.getLDongSignguCd())
            .lclsSystm1(dto.getLclsSystm1())
            .progresstype(dto.getProgresstype())
            .festivaltype(dto.getFestivaltype())
            .build();
    }

    public static FestivalDetailIntro toDetailIntro(DetailIntro2Dto dto) {
        return FestivalDetailIntro.builder()
            .agelimit(dto.getAgelimit())
            .bookingplace(dto.getBookingplace())
            .discountinfofestival(dto.getDiscountinfofestival())
            .eventhomepage(dto.getEventhomepage())
            .eventplace(dto.getEventplace())
            .festivalgrade(dto.getFestivalgrade())
            .placeinfo(dto.getPlaceinfo())
            .playtime(dto.getPlaytime())
            .program(dto.getProgram())
            .spendtimefestival(dto.getSpendtimefestival())
            .sponsor1(dto.getSponsor1())
            .sponsor1tel(dto.getSponsor1tel())
            .sponsor2(dto.getSponsor2())
            .sponsor2tel(dto.getSponsor2tel())
            .subevent(dto.getSubevent())
            .usetimefestival(dto.getUsetimefestival())
            .build();
    }

    public static FestivalImage toFestivalImage(DetailImage2Dto dto) {
        return FestivalImage.builder()
            .imgid(dto.getImgid())
            .originimgurl(dto.getOriginimgurl())
            .smallimageurl(dto.getSmallimageurl())
            .serialnum(dto.getSerialnum())
            .build();
    }

    public static Festival toEntity(
        SearchFestival2Dto searchFestival2Dto,
        DetailCommon2Dto detailCommon2Dto,
        DetailIntro2Dto detailIntro2Dto,
        List<DetailImage2Dto> detailImage2DtoList
    ) {
        FestivalBasicInfo basicInfo = toBasicInfo(searchFestival2Dto);
        FestivalDetailIntro detailIntro = toDetailIntro(detailIntro2Dto);
        List<FestivalImage> images = detailImage2DtoList.stream()
            .map(FestivalMapper::toFestivalImage)
            .collect(Collectors.toList());

        Festival festival = Festival.builder()
            .contentId(searchFestival2Dto.getContentid())
            .basicInfo(basicInfo)
            .overview(detailCommon2Dto.getOverview())
            .detailIntro(detailIntro)
            .detailImages(images)
            .build();

        images.forEach(img -> img.setFestival(festival));
        return festival;
    }
}
