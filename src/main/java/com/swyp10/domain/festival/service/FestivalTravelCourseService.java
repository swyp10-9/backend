package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.entity.FestivalTravelCourse;
import com.swyp10.domain.festival.repository.FestivalTravelCourseRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalTravelCourseService {

    private final FestivalTravelCourseRepository festivalTravelCourseRepository;

    public List<FestivalTravelCourse> getByFestivalId(Long festivalId) {
        if (festivalId == null || festivalId <= 0) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "Invalid festival ID: " + festivalId);
        }
        return festivalTravelCourseRepository.findByFestival_Id(festivalId);
    }

    @Transactional
    public FestivalTravelCourse addMapping(FestivalTravelCourse festivalTravelCourse) {
        if (festivalTravelCourse == null) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "FestivalTravelCourse cannot be null");
        }
        return festivalTravelCourseRepository.save(festivalTravelCourse);
    }

    @Transactional
    public void removeMapping(Long festivalId, Long courseId) {
        if (festivalId == null || festivalId <= 0) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "Invalid festival ID: " + festivalId);
        }
        if (courseId == null || courseId <= 0) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "Invalid course ID: " + courseId);
        }
        festivalTravelCourseRepository.deleteByFestival_IdAndTravelCourse_Id(festivalId, courseId);
    }
}
