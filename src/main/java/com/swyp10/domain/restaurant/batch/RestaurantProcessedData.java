package com.swyp10.domain.restaurant.batch;

import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailInfo2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailIntro2RestaurantDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class RestaurantProcessedData {
    private final AreaBasedList2RestaurantDto areaBasedDto;
    private final List<DetailInfo2RestaurantDto> detailInfo;
    private final DetailIntro2RestaurantDto detailIntro;
}
