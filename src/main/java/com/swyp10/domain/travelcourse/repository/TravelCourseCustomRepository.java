package com.swyp10.domain.travelcourse.repository;

import com.swyp10.domain.travelcourse.entity.TravelCourse;

import java.util.Optional;

public interface TravelCourseCustomRepository {
    Optional<TravelCourse> findOneByAreaCodeWithDetails(String areaCode);
}
