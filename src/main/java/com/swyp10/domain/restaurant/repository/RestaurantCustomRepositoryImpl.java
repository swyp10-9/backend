package com.swyp10.domain.restaurant.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp10.domain.restaurant.entity.QRestaurant;
import com.swyp10.domain.restaurant.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RestaurantCustomRepositoryImpl implements RestaurantCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Restaurant> findByAreaWithFilters(
        String areaCode,
        String category,
        Integer radiusMeters,
        Double centerLat,
        Double centerLng,
        String sort,
        Pageable pageable
    ) {
        QRestaurant restaurant = QRestaurant.restaurant;

        BooleanBuilder where = new BooleanBuilder();
        if (areaCode != null) {
            where.and(restaurant.basicInfo.areacode.eq(areaCode));
        }
        if (category != null && !category.isBlank()) {
            // todo
        }

        // distance 계산(미터): Haversine
        // 6371000 * acos( least(1.0, cos(radians(lat1))*cos(radians(lat2))*cos(radians(lng2)-radians(lng1)) + sin(radians(lat1))*sin(radians(lat2))) )
        var distanceExpr = (centerLat != null && centerLng != null)
            ? Expressions.numberTemplate(Double.class,
            "6371000 * acos(least(1.0, cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1}))))",
            centerLat, restaurant.basicInfo.mapy, restaurant.basicInfo.mapx, centerLng)
            : null;

        // 반경 필터
        if (radiusMeters != null && radiusMeters > 0 && distanceExpr != null) {
            where.and(distanceExpr.loe(radiusMeters.doubleValue()));
        }

        // 정렬 파싱
        OrderSpecifier<?> orderSpecifier = buildOrderSpecifier(sort, restaurant, distanceExpr);

        // content
        List<Restaurant> content = queryFactory
            .selectFrom(restaurant)
            .where(where)
            .orderBy(orderSpecifier)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(restaurant.count())
            .from(restaurant)
            .where(where)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == 0 ? 0 : total);
    }

    private OrderSpecifier<?> buildOrderSpecifier(String sort, QRestaurant restaurant, com.querydsl.core.types.Expression<Double> distanceExpr) {
        // 기본 정렬: name ASC
        if (sort == null || sort.isBlank()) {
            return new OrderSpecifier<>(Order.ASC, restaurant.basicInfo.title);
        }

        String[] parts = sort.split(",", 2);
        String key = parts[0].trim().toLowerCase();
        String direction = (parts.length > 1 ? parts[1].trim().toLowerCase() : "asc");
        Order order = direction.equals("desc") ? Order.DESC : Order.ASC;

        return switch (key) {
            case "distance" -> {
                // distance 정렬 요청인데 좌표 없으면 name ASC fallback
                if (distanceExpr == null) yield new OrderSpecifier<>(Order.ASC, restaurant.basicInfo.title);
                yield new OrderSpecifier<>(order, distanceExpr);
            }
            default -> new OrderSpecifier<>(Order.ASC, restaurant.basicInfo.title);
        };
    }
}
