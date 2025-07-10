package com.swyp10.global.vo;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@Embeddable
@NoArgsConstructor(access = PROTECTED)
@Getter
public class What3Words {

    private String word1;
    private String word2;
    private String word3;

    public What3Words(String word1, String word2, String word3) {
        this.word1 = word1;
        this.word2 = word2;
        this.word3 = word3;
    }

    @Override
    public String toString() {
        return word1 + "." + word2 + "." + word3;
    }
}
