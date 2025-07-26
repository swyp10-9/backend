package com.swyp10.domain.festival.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FestivalPeriod {
    THIS_WEEK("이번주"),
    THIS_MONTH("이번달"),
    NEXT_MONTH("다음달"),
    ALL("전체");

    private final String displayName;

    FestivalPeriod(String value) { this.displayName = value; }

    @JsonValue
    public String getDisplayName() { return displayName; }

    @JsonCreator
    public static FestivalPeriod from(String value) {
        for (FestivalPeriod period : values()) {
            if (period.displayName.equalsIgnoreCase(value) || period.name().equalsIgnoreCase(value)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid FestivalPeriod: " + value);
    }
}
