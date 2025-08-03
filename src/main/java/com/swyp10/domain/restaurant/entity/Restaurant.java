package com.swyp10.domain.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "content_id", unique = true, nullable = false, length = 32)
    private String contentId;

    @Embedded
    private RestaurantBasicInfo basicInfo;

    @Embedded
    private RestaurantDetailInfo detailInfo;

    // 메뉴 정보 리스트 (detailInfo2)
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RestaurantMenu> menus = new ArrayList<>();

    public void updateBasicInfo(RestaurantBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public void updateDetailInfo(RestaurantDetailInfo detailInfo) {
        this.detailInfo = detailInfo;
    }

    // 연관 관계 메서드
    public void addMenu(RestaurantMenu menu) {
        menus.add(menu);
        menu.setRestaurant(this);
    }

    public void clearMenus() {
        for (RestaurantMenu menu : menus) {
            menu.setRestaurant(null);
        }
        menus.clear();
    }
}
