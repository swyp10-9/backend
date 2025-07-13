package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.entity.FestivalTravelCourse;
import com.swyp10.domain.festival.repository.FestivalTravelCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalTravelCourseService {

    private final FestivalTravelCourseRepository festivalTravelCourseRepository;

    public List<FestivalTravelCourse> getByFestivalId(long festivalId) {
        return festivalTravelCourseRepository.findByFestivalId(festivalId);
    }

    @Transactional
    public FestivalTravelCourse addMapping(FestivalTravelCourse festivalTravelCourse) {
        return festivalTravelCourseRepository.save(festivalTravelCourse);
    }

    @Transactional
    public void removeMapping(long festivalId, long courseId) {
        festivalTravelCourseRepository.deleteByFestivalIdAndTravelCourseId(festivalId, courseId);
    }
}
