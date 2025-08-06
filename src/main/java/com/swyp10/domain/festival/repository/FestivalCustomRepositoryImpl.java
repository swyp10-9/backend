package com.swyp10.domain.festival.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp10.domain.festival.dto.request.FestivalMapRequest;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.QFestival;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FestivalCustomRepositoryImpl implements FestivalCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FestivalSummaryResponse> findFestivalsForMap(FestivalMapRequest request, Pageable pageable) {
        QFestival festival = QFestival.festival;

        BooleanBuilder where = new BooleanBuilder();
        // 1. 상태 필터
        if (request.getStatus() != null && !request.getStatus().isAll()) {
            where.and(festival.status.eq(request.getStatus()));
        }
        // 2. 기간 필터
        if (request.getPeriod() != null && !request.getPeriod().isAll()) {
            LocalDate now = LocalDate.now();
            LocalDate start;
            LocalDate end;

            switch (request.getPeriod()) {
                case THIS_WEEK -> {
                    start = now.with(DayOfWeek.MONDAY);
                    end = now.with(DayOfWeek.SUNDAY);
                }
                case THIS_MONTH -> {
                    YearMonth ym = YearMonth.from(now);
                    start = ym.atDay(1);
                    end = ym.atEndOfMonth();
                }
                case NEXT_MONTH -> {
                    YearMonth nextMonth = YearMonth.from(now).plusMonths(1);
                    start = nextMonth.atDay(1);
                    end = nextMonth.atEndOfMonth();
                }
                default -> {
                    start = null;
                    end = null;
                }
            }
            // start~end 기간 내 겹치는 축제만 (축제의 종료일 >= start && 시작일 <= end)
            if (start != null && end != null) {
                where.and(festival.basicInfo.eventenddate.goe(start)
                    .and(festival.basicInfo.eventstartdate.loe(end)));
            }
        }
        // 3. withWhom, theme
        if (request.getWithWhom() != null && !request.getWithWhom().isAll()) {
            where.and(festival.withWhom.eq(request.getWithWhom()));
        }
        if (request.getTheme() != null && !request.getTheme().isAll()) {
            where.and(festival.theme.eq(request.getTheme()));
        }
        // 4. 좌표 필터(지도 내 포함)
        if (request.getLatTopLeft() != null && request.getLatBottomRight() != null &&
            request.getLngTopLeft() != null && request.getLngBottomRight() != null) {
            where.and(
                festival.basicInfo.mapy.castToNum(Double.class)
                    .between(request.getLatBottomRight(), request.getLatTopLeft())
                    .and(festival.basicInfo.mapx.castToNum(Double.class)
                        .between(request.getLngTopLeft(), request.getLngBottomRight())));

        }

        List<Festival> content = queryFactory
            .selectFrom(festival)
            .where(where)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(festival.createdAt.desc())
            .fetch();

        // totalCount 쿼리
        long total = queryFactory
            .selectFrom(festival)
            .where(where)
            .fetchCount();

        // 엔티티 → DTO 변환 (builder로)
        List<FestivalSummaryResponse> dtos = content.stream()
            .map(FestivalSummaryResponse::from)
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, total);
    }
}
