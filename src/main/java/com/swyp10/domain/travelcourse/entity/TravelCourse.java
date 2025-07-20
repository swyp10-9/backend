package com.swyp10.domain.travelcourse.entity;

import com.swyp10.common.BaseTimeEntity;
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
    private Long id;

    private String title;

    @Column(name = "duration_hours")
    private int durationHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private TravelDifficulty difficultyLevel;

    public void updateCourse(String title, int durationHours, TravelDifficulty difficultyLevel) {
        this.title = title;
        this.durationHours = durationHours;
        this.difficultyLevel = difficultyLevel;
    }
}
