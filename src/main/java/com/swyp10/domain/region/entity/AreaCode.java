package com.swyp10.domain.region.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "area_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AreaCode {

    @Id
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;
}
