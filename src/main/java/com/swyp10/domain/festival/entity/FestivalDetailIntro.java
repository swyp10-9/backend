package com.swyp10.domain.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalDetailIntro {

    @Column(columnDefinition = "TEXT")
    private String agelimit;

    @Column(columnDefinition = "TEXT")
    private String bookingplace;

    @Column(columnDefinition = "TEXT")
    private String discountinfofestival;

    @Column(columnDefinition = "TEXT")
    private String eventhomepage;

    @Column(columnDefinition = "TEXT")
    private String eventplace;

    private String festivalgrade;

    @Column(columnDefinition = "TEXT")
    private String placeinfo;

    @Column(columnDefinition = "TEXT")
    private String playtime;

    @Column(columnDefinition = "TEXT")
    private String program;

    private String spendtimefestival;

    @Column(columnDefinition = "TEXT")
    private String sponsor1;

    @Column(columnDefinition = "TEXT")
    private String sponsor1tel;

    @Column(columnDefinition = "TEXT")
    private String sponsor2;

    @Column(columnDefinition = "TEXT")
    private String sponsor2tel;

    private String subevent;

    @Column(columnDefinition = "TEXT")
    private String usetimefestival;
}
