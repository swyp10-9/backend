package com.swyp10.domain.restaurant.entity;

import com.swyp10.common.BaseTimeEntity;
import com.swyp10.global.vo.Location;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Restaurant extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(nullable = false)
    private String name;

    @Column(length = 100)
    private String category;

    @Embedded
    private Location location;

    @Column(name = "naver_place_id", length = 100)
    private String naverPlaceId;

    @Column(length = 50)
    private String phone;

    @Column(name = "price_range", length = 50)
    private String priceRange;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // 수정 메서드
    public void updateInfo(String name, String category, Location location, String phone, String priceRange, String imageUrl) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.phone = phone;
        this.priceRange = priceRange;
        this.imageUrl = imageUrl;
    }

}
