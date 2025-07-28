package com.swyp10.domain.festival.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RegionFilter {
    SEOUL("서울"),
    GYEONGGI("경기"),
    GANGWON("강원"),
    CHUNGCHEONG("충청"),
    JEOLLA("전라"),
    GYEONGSANG("경상"),
    JEJU("제주"),
    ALL("전체");

    private final String displayName;

    RegionFilter(String value) {
        this.displayName = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static RegionFilter from(String value) {
        for (RegionFilter region : values()) {
            if (region.displayName.equalsIgnoreCase(value) || region.name().equalsIgnoreCase(value)) {
                return region;
            }
        }
        throw new IllegalArgumentException("Invalid RegionFilter: " + value);
    }
}
