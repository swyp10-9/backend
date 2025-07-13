package com.swyp10.domain.festival.repository;

import com.swyp10.domain.festival.entity.FestivalTravelCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalTravelCourseRepository extends JpaRepository<FestivalTravelCourse, Long> {

    public List<FestivalTravelCourse> findByFestivalId(long festivalId);

    public void deleteByFestivalIdAndTravelCourseId(long festivalId, long travelCourseId);
}
