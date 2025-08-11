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
    
    // 기본 반경 5km로 설정
    private static final int DEFAULT_RADIUS_METERS = 5000;
    // 최대 반경 50km로 제한
    private static final int MAX_RADIUS_METERS = 50000;

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
        
        // 1. 좌표 기반 거리 필터링 우선 적용
        if (centerLat != null && centerLng != null) {
            // 반경 설정: 요청값이 없거나 0이면 기본값 5km, 최대 50km로 제한
            int actualRadius = DEFAULT_RADIUS_METERS;
            if (radiusMeters != null && radiusMeters > 0) {
                actualRadius = Math.min(radiusMeters, MAX_RADIUS_METERS);
            }
            
            // Haversine 거리 계산식
            var distanceExpr = Expressions.numberTemplate(Double.class,
                "6371000 * acos(least(1.0, cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1}))))",
                centerLat, restaurant.basicInfo.mapy, restaurant.basicInfo.mapx, centerLng);
            
            // 거리 필터 적용
            where.and(distanceExpr.loe(actualRadius));
            
            System.out.println("=== 거리 필터 적용 ===");
            System.out.println("중심좌표: " + centerLat + ", " + centerLng);
            System.out.println("반경: " + actualRadius + "m (" + (actualRadius/1000.0) + "km)");
            
        } else {
            // 2. 좌표가 없으면 areacode 필터링 (백업)
            if (areaCode != null) {
                where.and(restaurant.basicInfo.areacode.eq(areaCode));
                System.out.println("=== areacode 필터 적용 (좌표 없음) ===");
                System.out.println("areaCode: " + areaCode);
            }
        }
        
        // distance 계산 (정렬용)
        var distanceExpr = (centerLat != null && centerLng != null)
            ? Expressions.numberTemplate(Double.class,
            "6371000 * acos(least(1.0, cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1}))))",
            centerLat, restaurant.basicInfo.mapy, restaurant.basicInfo.mapx, centerLng)
            : null;

        // 정렬 파싱
        OrderSpecifier<?> orderSpecifier = buildOrderSpecifier(sort, restaurant, distanceExpr);

        // content 조회
        List<Restaurant> content = queryFactory
            .selectFrom(restaurant)
            .where(where)
            .orderBy(orderSpecifier)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 총 개수
        Long totalL = queryFactory
            .select(restaurant.count())
            .from(restaurant)
            .where(where)
            .fetchOne();

        long total = (totalL != null) ? totalL : 0L;
        
        System.out.println("=== 조회 결과 ===");
        System.out.println("총 " + total + "개 음식점 중 " + content.size() + "개 조회");
        
        return new PageImpl<>(content, pageable, total);
    }
    
    /**
     * 카테고리를 contenttypeid로 매핑
     */

    private OrderSpecifier<?> buildOrderSpecifier(String sort, QRestaurant restaurant, com.querydsl.core.types.Expression<Double> distanceExpr) {
        // 기본 정렬: distance ASC (거리순)
        if (sort == null || sort.isBlank()) {
            if (distanceExpr != null) {
                return new OrderSpecifier<>(Order.ASC, distanceExpr);
            }
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
            case "name", "title" -> new OrderSpecifier<>(order, restaurant.basicInfo.title);
            default -> {
                // 기본값: 거리순 정렬
                if (distanceExpr != null) yield new OrderSpecifier<>(Order.ASC, distanceExpr);
                yield new OrderSpecifier<>(Order.ASC, restaurant.basicInfo.title);
            }
        };
    }
}
