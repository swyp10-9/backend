package com.swyp10.domain.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class RestaurantDetailInfo {

    private String chkcreditcardfood;

    @Column(columnDefinition = "TEXT")
    private String discountinfofood;

    private String firstmenu;

    private String infocenterfood;

    private String kidsfacility;

    private String opendatefood;

    @Column(columnDefinition = "TEXT")
    private String opentimefood;

    private String packing;

    private String parkingfood;

    private String reservationfood;

    private String restdatefood;

    @Column(columnDefinition = "TEXT")
    private String scalefood;

    private String seat;

    private String smoking;

    @Column(columnDefinition = "TEXT")
    private String treatmenu;

    private String lcnsno;
}
