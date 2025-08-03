package com.swyp10.domain.region.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ldong_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LdongCode {

    @Id
    @Column(length = 10)
    private String code;           // 예: "36110"

    @Column(nullable = false, length = 100)
    private String name;           // 동 이름 등

    @Column(length = 10)
    private String sigunguCode;    // 시군구 코드 연결

    @Column(length = 10)
    private String areaCode;
}
