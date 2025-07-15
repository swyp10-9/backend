package com.swyp10.global.vo;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Embeddable
@NoArgsConstructor(access = PROTECTED)
@Getter
public class What3Words {

    private String word1;
    private String word2;
    private String word3;

    public What3Words(String word1, String word2, String word3) {
        if (word1 == null || word2 == null || word3 == null) {
            throw new IllegalArgumentException("What3Words cannot contain null values");
        }
        if (word1.trim().isEmpty() || word2.trim().isEmpty() || word3.trim().isEmpty()) {
            throw new IllegalArgumentException("What3Words cannot contain empty values");
        }

        this.word1 = word1;
        this.word2 = word2;
        this.word3 = word3;
    }

    @Override
    public String toString() {
        return word1 + "." + word2 + "." + word3;
    }
}
