package com.swyp10.domain.festival.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FestivalPersonalityType {
    ENERGIZER("축제 에너자이저"),
    EXPLORER("문화 탐험가"),
    CURATOR("축제 큐레이터"),
    SOCIALIZER("축제 소셜라이저"),
    HEALER("축제 힐러");

    private final String displayName;

    FestivalPersonalityType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static FestivalPersonalityType from(String value) {
        for (FestivalPersonalityType type : values()) {
            if (type.displayName.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid FestivalPersonalityType: " + value);
    }
}

