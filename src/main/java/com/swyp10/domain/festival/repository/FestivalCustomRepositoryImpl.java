package com.swyp10.domain.festival.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp10.domain.festival.dto.request.FestivalCalendarRequest;
import com.swyp10.domain.festival.dto.request.FestivalMapRequest;
import com.swyp10.domain.festival.dto.request.FestivalPersonalTestRequest;
import com.swyp10.domain.festival.dto.request.FestivalSearchRequest;
import com.swyp10.domain.festival.dto.response.FestivalDailyCountResponse;
import com.swyp10.domain.festival.dto.response.FestivalMonthlyTopResponse;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.QFestival;
import com.swyp10.domain.festival.entity.QFestivalStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
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
                festival.basicInfo.mapy.isNotNull().and(
                    festival.basicInfo.mapx.isNotNull()).and(
                festival.basicInfo.mapy.castToNum(Double.class)
                    .between(request.getLatBottomRight(), request.getLatTopLeft())
                    .and(festival.basicInfo.mapx.castToNum(Double.class)
                        .between(request.getLngTopLeft(), request.getLngBottomRight()))));

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

    @Override
    public Page<FestivalSummaryResponse> findFestivalsForCalendar(FestivalCalendarRequest request, Pageable pageable) {
        QFestival festival = QFestival.festival;
        BooleanBuilder where = new BooleanBuilder();

        // 지역 필터
        if (request.getRegion() != null && !request.getRegion().isAll()) {
            where.and(festival.regionFilter.eq(request.getRegion()));
        }
        // 누구랑 필터
        if (request.getWithWhom() != null && !request.getWithWhom().isAll()) {
            where.and(festival.withWhom.eq(request.getWithWhom()));
        }
        // 테마 필터
        if (request.getTheme() != null && !request.getTheme().isAll()) {
            where.and(festival.theme.eq(request.getTheme()));
        }
        // 날짜(달력) 필터: date가 축제기간에 포함되는 축제만
        if (request.getDate() != null) {
            LocalDate date = request.getDate();
            where.and(festival.basicInfo.eventstartdate.loe(date)
                .and(festival.basicInfo.eventenddate.goe(date)));
        }

        List<Festival> content = queryFactory
            .selectFrom(festival)
            .where(where)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(festival.basicInfo.eventstartdate.asc())
            .fetch();

        long total = queryFactory
            .selectFrom(festival)
            .where(where)
            .fetchCount();

        List<FestivalSummaryResponse> dtos = content.stream()
            .map(FestivalSummaryResponse::from)
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    public List<FestivalDailyCountResponse.DailyCount> findDailyFestivalCounts(LocalDate start, LocalDate end) {
        QFestival festival = QFestival.festival;

        // DB에 한 번에 일자별 개수를 구하는 쿼리
        List<FestivalDailyCountResponse.DailyCount> result = new ArrayList<>();
        LocalDate date = start;
        while (!date.isAfter(end)) {
            Long count = queryFactory.select(festival.count())
                .from(festival)
                .where(
                    festival.basicInfo.eventstartdate.loe(date)
                        .and(festival.basicInfo.eventenddate.goe(date))
                )
                .fetchOne();
            result.add(new FestivalDailyCountResponse.DailyCount(date, count == null ? 0 : count.intValue()));
            date = date.plusDays(1);
        }
        return result;
    }

    @Override
    public Page<FestivalSummaryResponse> findFestivalsForPersonalTest(FestivalPersonalTestRequest request, Pageable pageable) {
        QFestival festival = QFestival.festival;

        BooleanBuilder where = new BooleanBuilder();

        if (request.getPersonalityType() != null) {
            where.and(festival.personalityType.eq(request.getPersonalityType()));
        }

        List<Festival> content = queryFactory
            .selectFrom(festival)
            .where(where)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(festival.createdAt.desc())
            .fetch();

        long total = queryFactory
            .selectFrom(festival)
            .where(where)
            .fetchCount();

        List<FestivalSummaryResponse> dtos = content.stream()
            .map(FestivalSummaryResponse::from)
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    public Page<FestivalSummaryResponse> searchFestivals(FestivalSearchRequest request, Pageable pageable) {
        QFestival festival = QFestival.festival;

        BooleanBuilder where = new BooleanBuilder();
        String search = request.getSearchParam();
        if (search != null && !search.isBlank()) {
            BooleanExpression titleLike = festival.basicInfo.title.containsIgnoreCase(search);
            BooleanExpression descLike = festival.overview.containsIgnoreCase(search);
            BooleanExpression addrLike = festival.basicInfo.addr1.containsIgnoreCase(search);
            where.and(titleLike.or(descLike).or(addrLike));
        }

        List<Festival> content = queryFactory
            .selectFrom(festival)
            .where(where)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(festival.createdAt.desc())
            .fetch();

        long total = queryFactory
            .selectFrom(festival)
            .where(where)
            .fetchCount();

        List<FestivalSummaryResponse> dtos = content.stream()
            .map(FestivalSummaryResponse::from)
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    public List<FestivalMonthlyTopResponse> findTop5ByViewCountInCurrentMonth(LocalDate startOfMonth, LocalDate endOfMonth) {
        QFestival festival = QFestival.festival;
        QFestivalStatistics statistics = QFestivalStatistics.festivalStatistics;

        BooleanBuilder where = new BooleanBuilder();

        // 현재 월에 진행되는 축제 조건 (기존 달력 필터 로직과 동일한 방식)
        where.and(festival.basicInfo.eventstartdate.loe(endOfMonth)
            .and(festival.basicInfo.eventenddate.goe(startOfMonth)));

        List<Festival> content = queryFactory
            .selectFrom(festival)
            .leftJoin(statistics).on(festival.festivalId.eq(statistics.festivalId))
            .where(where)
            .orderBy(statistics.viewCount.coalesce(0).desc())
            .limit(5)
            .fetch();

        // 엔티티 → FestivalMonthlyTopResponse DTO 변환 (overview 포함)
        List<FestivalMonthlyTopResponse> dtos = content.stream()
            .map(FestivalMonthlyTopResponse::from)
            .collect(Collectors.toList());

        return dtos;
    }
}
