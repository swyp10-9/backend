package com.swyp10.domain.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalDetailIntro {
    private String agelimit;
    private String bookingplace;
    private String discountinfofestival;
    private String eventhomepage;
    private String eventplace;
    private String festivalgrade;
    private String placeinfo;
    private String playtime;
    private String program;
    private String spendtimefestival;
    private String sponsor1;
    private String sponsor1tel;
    private String sponsor2;
    private String sponsor2tel;
    private String subevent;
    private String usetimefestival;
}
