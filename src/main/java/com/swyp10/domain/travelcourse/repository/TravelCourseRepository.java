package com.swyp10.domain.travelcourse.repository;

import com.swyp10.domain.travelcourse.entity.TravelCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelCourseRepository extends JpaRepository<TravelCourse, Long>, TravelCourseCustomRepository {
    Optional<TravelCourse> findByContentId(String contentId);
}
