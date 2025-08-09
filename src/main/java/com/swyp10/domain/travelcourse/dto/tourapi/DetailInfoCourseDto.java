package com.swyp10.domain.travelcourse.dto.tourapi;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailInfoCourseDto {
    private String subnum;
    private String subcontentid;
    private String subname;
    private String subdetailoverview;
    private String subdetailimg;
}
