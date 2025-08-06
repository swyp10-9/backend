package com.swyp10.domain.festival.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FestivalStatus {
    ONGOING("진행중"),
    UPCOMING("예정"),
    ENDED("종료"),
    ALL("전체");

    private final String displayName;

    FestivalStatus(String value) { this.displayName = value; }

    public String getDisplayName() { return displayName; }

    @JsonCreator
    public static FestivalStatus from(String value) {
        for (FestivalStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid FestivalStatus: " + value);
    }

    public boolean isAll() {
        return this == ALL;
    }
}
