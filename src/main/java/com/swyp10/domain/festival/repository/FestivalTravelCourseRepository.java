package com.swyp10.domain.festival.repository;

import com.swyp10.domain.festival.entity.FestivalTravelCourse;
import com.swyp10.domain.festival.entity.FestivalTravelCourseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface FestivalTravelCourseRepository extends JpaRepository<FestivalTravelCourse, FestivalTravelCourseId> {

    List<FestivalTravelCourse> findByFestival_Id(Long festivalId);

    @Modifying
    void deleteByFestival_IdAndTravelCourse_Id(Long festivalId, Long travelCourseId);

}
