package com.swyp10.domain.festival.batch;

import com.swyp10.domain.festival.dto.tourapi.DetailCommon2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailImage2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailIntro2Dto;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class FestivalProcessedData {
    private final SearchFestival2Dto searchDto;
    private final DetailCommon2Dto commonDto;
    private final DetailIntro2Dto introDto;
    private final List<DetailImage2Dto> images;
}
