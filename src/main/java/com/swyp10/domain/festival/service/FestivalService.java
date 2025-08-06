package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.FestivalDailyCountResponse;
import com.swyp10.domain.festival.dto.response.FestivalDetailResponse;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import com.swyp10.domain.festival.dto.tourapi.DetailCommon2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailImage2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailIntro2Dto;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.mapper.FestivalMapper;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalRepository festivalRepository;

    @Transactional
    public Festival saveOrUpdateFestival(
        SearchFestival2Dto searchFestival2Dto,
        DetailCommon2Dto detailCommon2Dto,
        DetailIntro2Dto detailIntro2Dto,
        List<DetailImage2Dto> detailImage2DtoList
    ) {
        return festivalRepository.findByContentId(searchFestival2Dto.getContentid())
            .map(existing -> {
                existing.clearDetailImages();
                existing.updateOverview(detailCommon2Dto.getOverview());
                existing.updateDetailIntro(FestivalMapper.toDetailIntro(detailIntro2Dto));
                existing.updateBasicInfo(FestivalMapper.toBasicInfo(searchFestival2Dto));

                detailImage2DtoList.stream()
                    .map(FestivalMapper::toFestivalImage)
                    .forEach(existing::addDetailImage);

                return existing;
            })
            .orElseGet(() ->
                festivalRepository.save(FestivalMapper.toEntity(
                    searchFestival2Dto, detailCommon2Dto, detailIntro2Dto, detailImage2DtoList
                ))
            );
    }

    public Festival findByFestivalId(Long festivalId) {
        return festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND));
    }


    public Festival findByContentId(String contentId) {
        return festivalRepository.findByContentId(contentId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

    public boolean existsByContentId(String contentId) {
        return festivalRepository.findByContentId(contentId).isPresent();
    }

    @Transactional
    public void deleteByFestivalId(Long festivalId) {
        festivalRepository.deleteById(festivalId);
    }

    public FestivalListResponse getFestivalsForMap(FestivalMapRequest request) {
        int page = request.getPage();
        int size = request.getSize();
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForMap(request, pageRequest);

        return FestivalListResponse.builder()
            .content(result.getContent())
            .page(result.getNumber())
            .size(result.getSize())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .first(result.isFirst())
            .last(result.isLast())
            .empty(result.isEmpty())
            .build();
    }

    public FestivalListResponse getFestivalsForCalendar(FestivalCalendarRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        Page<FestivalSummaryResponse> page = festivalRepository.findFestivalsForCalendar(request, pageRequest);

        return FestivalListResponse.builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .empty(page.isEmpty())
            .build();
    }

    public FestivalDailyCountResponse getDailyFestivalCount(LocalDate startDate, LocalDate endDate) {
        List<FestivalDailyCountResponse.DailyCount> dailyCounts =
            festivalRepository.findDailyFestivalCounts(startDate, endDate);

        return FestivalDailyCountResponse.builder()
            .startDate(startDate)
            .endDate(endDate)
            .dailyCounts(dailyCounts)
            .build();
    }

    public FestivalListResponse getFestivalsForPersonalTest(FestivalPersonalTestRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());

        Page<FestivalSummaryResponse> page = festivalRepository.findFestivalsForPersonalTest(request, pageRequest);

        return FestivalListResponse.builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .empty(page.isEmpty())
            .build();
    }

    public FestivalListResponse searchFestivals(FestivalSearchRequest request) {
        return null;
    }

    public FestivalListResponse getMyPageFestivals(FestivalMyPageRequest request) {
        return null;
    }

    public FestivalDetailResponse getFestivalDetail(Long festivalId) {
        return null;
    }
}
