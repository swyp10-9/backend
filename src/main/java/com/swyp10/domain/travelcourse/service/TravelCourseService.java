package com.swyp10.domain.travelcourse.service;

import com.swyp10.domain.travelcourse.entity.TravelCourse;
import com.swyp10.domain.travelcourse.repository.TravelCourseRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelCourseService {

    private final TravelCourseRepository travelCourseRepository;

    public TravelCourse getTravelCourse(Long courseId) {
        return travelCourseRepository.findById(courseId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "TravelCourse not found: " + courseId));
    }

    @Transactional
    public TravelCourse createTravelCourse(TravelCourse travelCourse) {
        return travelCourseRepository.save(travelCourse);
    }

    @Transactional
    public void deleteTravelCourse(Long courseId) {
        travelCourseRepository.deleteById(courseId);
    }
}
