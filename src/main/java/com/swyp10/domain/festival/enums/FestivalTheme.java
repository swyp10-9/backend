package com.swyp10.domain.festival.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FestivalTheme {
    CULTURE_ART("문화/예술"),
    FOOD("음식/미식"),
    MUSIC("음악/공연"),
    NATURE("자연/체험"),
    TRADITION("전통/역사"),
    ALL("전체");

    private final String displayName;

    FestivalTheme(String value) { this.displayName = value; }

    public String getDisplayName() { return displayName; }

    @JsonCreator
    public static FestivalTheme from(String value) {
        for (FestivalTheme theme : values()) {
            if (theme.displayName.equalsIgnoreCase(value) || theme.name().equalsIgnoreCase(value)) {
                return theme;
            }
        }
        throw new IllegalArgumentException("Invalid FestivalTheme: " + value);
    }

    public boolean isAll() {
        return this == ALL;
    }
}
