package com.swyp10.domain.travelcourse.mapper;

import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import com.swyp10.domain.travelcourse.entity.TravelCourse;
import com.swyp10.domain.travelcourse.entity.TravelCourseBasicInfo;
import com.swyp10.domain.travelcourse.entity.TravelCourseDetailInfo;

import java.util.List;

public class TravelCourseMapper {

    public static TravelCourseBasicInfo toBasicInfo(SearchTravelCourseDto dto) {
        return TravelCourseBasicInfo.builder()
            .addr1(dto.getAddr1())
            .areacode(dto.getAreacode())
            .createdtime(dto.getCreatedtime())
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
            .build();
    }

    public static TravelCourseDetailInfo toDetailInfo(DetailInfoCourseDto detailDto) {
        return TravelCourseDetailInfo.builder()
            .subnum(detailDto.getSubnum())
            .subcontentid(detailDto.getSubcontentid())
            .subname(detailDto.getSubname())
            .subdetailoverview(detailDto.getSubdetailoverview())
            .subdetailimg(detailDto.getSubdetailimg())
            .build();
    }

    public static TravelCourse toEntity(SearchTravelCourseDto searchDto, List<DetailInfoCourseDto> detailInfoDtos) {
        TravelCourse travelCourse = TravelCourse.builder()
            .contentId(searchDto.getContentid())
            .basicInfo(toBasicInfo(searchDto))
            .build();

        if (detailInfoDtos != null) {
            for (DetailInfoCourseDto detailDto : detailInfoDtos) {
                TravelCourseDetailInfo detailInfo = toDetailInfo(detailDto);
                travelCourse.addDetailInfo(detailInfo);
            }
        }
        return travelCourse;
    }
}
