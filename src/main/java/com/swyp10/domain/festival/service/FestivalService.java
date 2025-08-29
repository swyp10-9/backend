package com.swyp10.domain.festival.service;

import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.*;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final UserBookmarkRepository userBookmarkRepository;

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
                existing.updateDetailIntro(FestivalMapper.toDetailIntroFromThreeDto(null, detailCommon2Dto, detailIntro2Dto));
                existing.updateBasicInfo(FestivalMapper.toBasicInfoFromThreeDto(searchFestival2Dto, detailCommon2Dto, detailIntro2Dto));

                detailImage2DtoList.stream()
                    .map(FestivalMapper::toFestivalImage)
                    .forEach(existing::addDetailImage);

                return existing;
            })
            .orElseGet(() -> {
                // 새로운 Festival 생성
                Festival newFestival = FestivalMapper.toEntity(
                    searchFestival2Dto, detailCommon2Dto, detailIntro2Dto, detailImage2DtoList
                );

                Festival savedFestival = festivalRepository.save(newFestival);

                // Statistics 초기화
                savedFestival.initializeStatistics();

                return festivalRepository.save(savedFestival);
            });
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

    /**
     * 전체 축제 데이터 개수 조회 (배치용)
     */
    public long getTotalFestivalCount() {
        return festivalRepository.count();
    }

    @Transactional
    public void deleteByFestivalId(Long festivalId) {
        festivalRepository.deleteById(festivalId);
    }

    public FestivalListResponse getFestivalsForMap(Long userId, FestivalMapRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForMap(request, pageRequest);

        return buildListResponseWithBookmarks(userId, result);
    }

    public FestivalListResponse getFestivalsForCalendar(Long userId, FestivalCalendarRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForCalendar(request, pageRequest);

        return buildListResponseWithBookmarks(userId, result);
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

    public FestivalMonthlyTopListResponse getMonthlyTopFestivals(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<FestivalMonthlyTopResponse> topFestivals = festivalRepository.findTop5ByViewCountInCurrentMonth(startOfMonth, endOfMonth);

        // 로그인한 사용자인 경우에만 북마크 상태 확인
        if (userId != null && !topFestivals.isEmpty()) {
            List<Long> festivalIds = topFestivals.stream()
                .map(FestivalMonthlyTopResponse::getId)
                .toList();

            Set<Long> bookmarkedIds = userBookmarkRepository.findBookmarkedFestivalIds(userId, festivalIds);

            topFestivals.forEach(festival -> {
                boolean isBookmarked = bookmarkedIds.contains(festival.getId());
                festival.setBookmarked(isBookmarked);
            });
        } else {
            // 로그인하지 않은 사용자는 모든 북마크를 false로 설정
            topFestivals.forEach(festival -> festival.setBookmarked(false));
        }

        return FestivalMonthlyTopListResponse.builder()
            .content(topFestivals)
            .page(0)
            .size(5)
            .totalElements((long) topFestivals.size())
            .totalPages(1)
            .first(true)
            .last(true)
            .empty(topFestivals.isEmpty())
            .build();
    }

    public FestivalListResponse getFestivalsForPersonalTest(Long userId, FestivalPersonalTestRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());

        Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForPersonalTest(request, pageRequest);

        return buildListResponseWithBookmarks(userId, result);
    }

    public FestivalListResponse searchFestivals(Long userId, FestivalSearchRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        Page<FestivalSummaryResponse> result = festivalRepository.searchFestivals(request, pageRequest);

        return buildListResponseWithBookmarks(userId, result);
    }

    public FestivalListResponse getMyBookmarkedFestivals(Long userId, FestivalMyPageRequest request) {

        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        if (request.getSort() != null && !request.getSort().isBlank()) {
            sort = Sort.by(request.getSort());
        }
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<FestivalSummaryResponse> page = userBookmarkRepository.findBookmarkedFestivals(userId, pageable);
        
        return buildListResponseWithBookmarks(userId, page);
    }

    private FestivalListResponse buildListResponseWithBookmarks(Long userId, Page<FestivalSummaryResponse> page) {
        // 북마크 상태 설정
        if (userId != null && !page.getContent().isEmpty()) {
            List<Long> festivalIds = page.getContent().stream()
                .map(FestivalSummaryResponse::getId)
                .toList();

            Set<Long> bookmarkedIds = userBookmarkRepository.findBookmarkedFestivalIds(userId, festivalIds);

            page.getContent().forEach(festival -> {
                boolean isBookmarked = bookmarkedIds.contains(festival.getId());
                festival.setBookmarked(isBookmarked);
            });
        } else {
            // userId가 null이면 모든 북마크를 false로 설정
            page.getContent().forEach(festival -> festival.setBookmarked(false));
        }

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


}
