package com.swyp10.domain.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RestaurantMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private String serialnum;

    private String menuname;

    private String menuprice;

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
