package com.swyp10.domain.restaurant.dto.tourapi;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailInfo2RestaurantDto {
    private String serialnum;
    private String menuname;
    private String menuprice;
}
