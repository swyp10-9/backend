package com.swyp10.domain.travelcourse.service;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.domain.travelcourse.dto.request.FestivalTravelCoursePageRequest;
import com.swyp10.domain.travelcourse.dto.response.FestivalTravelCourseListResponse;
import com.swyp10.domain.travelcourse.dto.response.FestivalTravelCourseResponse;
import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import com.swyp10.domain.travelcourse.entity.TravelCourse;
import com.swyp10.domain.travelcourse.entity.TravelCourseDetailInfo;
import com.swyp10.domain.travelcourse.mapper.TravelCourseMapper;
import com.swyp10.domain.travelcourse.repository.TravelCourseRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelCourseService {

    private final FestivalRepository festivalRepository;
    private final TravelCourseRepository travelCourseRepository;

    @Transactional
    public TravelCourse saveOrUpdateTravelCourse(SearchTravelCourseDto searchDto, List<DetailInfoCourseDto> detailInfoDtos) {
        return travelCourseRepository.findByContentId(searchDto.getContentid())
            .map(existing -> updateExistingTravelCourse(existing, searchDto, detailInfoDtos))
            .orElseGet(() -> travelCourseRepository.save(TravelCourseMapper.toEntity(searchDto, detailInfoDtos)));
    }

    /**
     * 전체 여행코스 데이터 개수 조회 (배치용)
     */
    public long getTotalTravelCourseCount() {
        return travelCourseRepository.count();
    }

    private TravelCourse updateExistingTravelCourse(TravelCourse existing,
                                                    SearchTravelCourseDto searchDto,
                                                    List<DetailInfoCourseDto> detailInfoDtos) {

        existing.updateBasicInfo(TravelCourseMapper.toBasicInfo(searchDto));
        existing.clearDetailInfos();

        if (detailInfoDtos != null) {
            detailInfoDtos.forEach(detailDto -> {
                var detailInfo = TravelCourseMapper.toDetailInfo(detailDto);
                existing.addDetailInfo(detailInfo);
            });
        }
        return existing;
    }

    @Transactional(readOnly = true)
    public TravelCourse findByContentId(String contentId) {
        return travelCourseRepository.findByContentId(contentId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.TRAVEL_COURSE_NOT_FOUND));
    }

    @Transactional
    public void deleteTravelCourse(Long courseId) {
        travelCourseRepository.deleteById(courseId);
    }

    public FestivalTravelCourseListResponse getFestivalTravelCourses(FestivalTravelCoursePageRequest request) {
        // 1) festivalId로 Festival 조회 → areacode 획득
        Festival festival = festivalRepository.findById(request.getFestivalId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND,
                "Festival not found: " + request.getFestivalId()));

        String areaCode = (festival.getBasicInfo() != null) ? festival.getBasicInfo().getAreacode() : null;
        if (areaCode == null || areaCode.isBlank()) {
            throw new ApplicationException(ErrorCode.TRAVEL_COURSE_NOT_FOUND,
                "Festival has no areaCode: " + request.getFestivalId());
        }

        // 2) 해당 areacode로 TravelCourse 1건 + detailInfos 조회
        TravelCourse course = travelCourseRepository.findOneByAreaCodeWithDetails(areaCode)
            .orElse(null);

        if (course == null) {
            return FestivalTravelCourseListResponse.builder()
                .courses(Collections.emptyList())
                .nearbyAttractions(Collections.emptyList())
                .build();
        }

        // 3) 코스 1건을 courses 리스트로 변환
        List<FestivalTravelCourseResponse> courses = List.of(
            FestivalTravelCourseResponse.builder()
                .id(course.getCourseId())
                .title(course.getBasicInfo() != null ? course.getBasicInfo().getTitle() : null)
                .time(null) // todo
                .build()
        );

        // 4) detailInfos
        String mapx = (course.getBasicInfo() != null) ? course.getBasicInfo().getMapx() : null;
        String mapy = (course.getBasicInfo() != null) ? course.getBasicInfo().getMapy() : null;

        List<FestivalTravelCourseListResponse.NearbyAttractionResponse> nearby = course.getDetailInfos().stream()
            .map(di -> toNearbyAttraction(di, mapx, mapy))
            .toList();

        return FestivalTravelCourseListResponse.builder()
            .courses(courses)
            .nearbyAttractions(nearby)
            .build();
    }

    private FestivalTravelCourseListResponse.NearbyAttractionResponse toNearbyAttraction(
        TravelCourseDetailInfo di, String fallbackMapx, String fallbackMapy) {

        return FestivalTravelCourseListResponse.NearbyAttractionResponse.builder()
            .name(di.getSubname())
            .thumbnail(di.getSubdetailimg())
            // todo (추후 좌표 필드 확장 권장)
            .mapx(fallbackMapx)
            .mapy(fallbackMapy)
            // todo 상세 페이지 URL 정보가 없으므로 null
            .descriptionUrl(null)
            .build();
    }
}
