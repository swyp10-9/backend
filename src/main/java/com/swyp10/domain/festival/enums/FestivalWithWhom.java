package com.swyp10.domain.festival.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FestivalWithWhom {
    FAMILY("가족"),
    COUPLE("커플"),
    PARENTS("부모님"),
    PET("반려견"),
    FRIENDS("친구"),
    ALL("전체");

    private final String displayName;

    FestivalWithWhom(String value) { this.displayName = value; }

    public String getDisplayName() { return displayName; }

    @JsonCreator
    public static FestivalWithWhom from(String value) {
        for (FestivalWithWhom withWhom : values()) {
            if (withWhom.displayName.equalsIgnoreCase(value) || withWhom.name().equalsIgnoreCase(value)) {
                return withWhom;
            }
        }
        throw new IllegalArgumentException("Invalid FestivalWithWhom: " + value);
    }
}
