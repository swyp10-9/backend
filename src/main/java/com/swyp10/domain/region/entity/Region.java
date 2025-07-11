package com.swyp10.domain.region.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "copy_of_regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Region extends BaseTimeEntity {

    @Id
    @Column(name = "region_code")
    private Integer regionCode;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Column(name = "parent_code", length = 50)
    private String parentCode;

    @OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Festival> festivals = new ArrayList<>();

}
