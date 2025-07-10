package com.swyp10.domain.festival.entity;

public enum FestivalTheme {
    CULTURE("문화"),
    FOOD("음식"),
    MUSIC("음악"),
    ART("예술"),
    HISTORY("역사"),
    FAMILY("가족"),
    NATURE("자연"),
    ACTIVITY("활동");

    private final String displayName;

    FestivalTheme(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
