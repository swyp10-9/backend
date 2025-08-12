package com.swyp10.domain.travelcourse.batch;

import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TravelCourseProcessedData {
    private final SearchTravelCourseDto searchDto;
    private final DetailInfoCourseDto detailInfo;
}
