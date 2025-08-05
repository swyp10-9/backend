package com.swyp10.domain.travelcourse.service;

import com.swyp10.domain.travelcourse.dto.request.FestivalTravelCoursePageRequest;
import com.swyp10.domain.travelcourse.dto.response.FestivalTravelCourseListResponse;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelCourseService {

    private final TravelCourseRepository travelCourseRepository;

    @Transactional
    public TravelCourse saveOrUpdateTravelCourse(SearchTravelCourseDto searchDto, List<DetailInfoCourseDto> detailInfoDtos) {
        return travelCourseRepository.findByContentId(searchDto.getContentid())
            .map(existing -> updateExistingTravelCourse(existing, searchDto, detailInfoDtos))
            .orElseGet(() -> travelCourseRepository.save(TravelCourseMapper.toEntity(searchDto, detailInfoDtos)));
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

    public TravelCourse getTravelCourse(Long courseId) {
        return travelCourseRepository.findById(courseId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.TRAVEL_COURSE_NOT_FOUND));
    }

    @Transactional
    public TravelCourse createTravelCourse(TravelCourse travelCourse) {
        return travelCourseRepository.save(travelCourse);
    }

    @Transactional
    public void deleteTravelCourse(Long courseId) {
        travelCourseRepository.deleteById(courseId);
    }

    public FestivalTravelCourseListResponse getFestivalTravelCourses(FestivalTravelCoursePageRequest request) {
        return null;
    }
}
