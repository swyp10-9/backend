package com.swyp10.domain.festival.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FestivalTravelCourseId implements Serializable {

    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "course_id")
    private Long courseId;
}
