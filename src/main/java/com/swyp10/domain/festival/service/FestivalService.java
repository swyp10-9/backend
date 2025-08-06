package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.dto.request.*;
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
                // overview, detailIntro update
                existing.clearDetailImages();

                // 기존 필드도 업데이트 가능하게 추가 (필요에 따라 setter 추가 권장)
                existing.updateOverview(detailCommon2Dto.getOverview());
                existing.updateDetailIntro(FestivalMapper.toDetailIntro(detailIntro2Dto));
                existing.updateBasicInfo(FestivalMapper.toBasicInfo(searchFestival2Dto));

                // 이미지들 다시 추가
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

    /**
     * 모든 축제 리스트 조회 (페이지네이션 필요시 Pageable 파라미터 추가)
     */
    public List<Festival> findAllFestivals() {
        return festivalRepository.findAll();
    }

    public boolean existsByContentId(String contentId) {
        return festivalRepository.findByContentId(contentId).isPresent();
    }

    @Transactional
    public Festival createFestival(Festival festival) {
        return festivalRepository.save(festival);
    }

    @Transactional
    public void deleteByFestivalId(Long festivalId) {
        festivalRepository.deleteById(festivalId);
    }

    public FestivalListResponse getFestivalsForMap(FestivalMapRequest request) {
        // 페이지 정보 추출 (0-based page index)
        int page = request.getPage();
        int size = request.getSize();
        PageRequest pageRequest = PageRequest.of(page, size);

        // 커스텀 레포지토리 메서드 호출 (Page<FestivalSummaryResponse> 반환)
        Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForMap(request, pageRequest);

        // PageResponse로 변환 후 FestivalListResponse로 래핑
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
        return null;
    }

    public FestivalListResponse getFestivalsForPersonalTest(FestivalPersonalTestRequest request) {
        return null;
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
