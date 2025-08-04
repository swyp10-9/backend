package com.swyp10.domain.region.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "ldong_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LdongCode {

    @Id
    @NotBlank
    @Size(max = 10)
    private String code;           // 예: "36110"

    @NotBlank
    @Size(max = 100)
    private String name;           // 동 이름 등

    @Size(max = 10)
    private String sigunguCode;    // 시군구 코드 연결

    @Size(max = 10)
    private String areaCode;
}
