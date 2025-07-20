package com.swyp10.domain.festival.entity;

import com.swyp10.domain.travelcourse.entity.TravelCourse;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "festival_travel_course")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FestivalTravelCourse {

    @EmbeddedId
    private FestivalTravelCourseId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("festivalId")
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private TravelCourse travelCourse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
        this.id.setFestivalId(festival.getId());
    }

    public void setTravelCourse(TravelCourse travelCourse) {
        this.travelCourse = travelCourse;
        this.id.setCourseId(travelCourse.getId());
    }

}
