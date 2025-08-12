package com.swyp10.domain.travelcourse.batch;

import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class TravelCourseProcessedData {
    private final SearchTravelCourseDto searchDto;
    private final List<DetailInfoCourseDto> detailInfo;
}
