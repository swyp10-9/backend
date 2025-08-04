package com.swyp10.domain.travelcourse.dto.tourapi;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailInfoCourseDto {
    private String serialnum;
    private String coursename;
    private String coursedesc;
    private String coursedist;
    private String coursestime;
}
