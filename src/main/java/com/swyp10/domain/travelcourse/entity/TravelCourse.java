package com.swyp10.domain.travelcourse.entity;

import com.swyp10.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "travel_courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelCourse extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    private String title;

    @Column(name = "duration_hours")
    private int durationHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private TravelDifficulty difficultyLevel;

}
