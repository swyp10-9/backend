package com.swyp10.domain.travelcourse.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp10.domain.travelcourse.entity.QTravelCourse;
import com.swyp10.domain.travelcourse.entity.QTravelCourseDetailInfo;
import com.swyp10.domain.travelcourse.entity.TravelCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TravelCourseCustomRepositoryImpl implements TravelCourseCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<TravelCourse> findOneByAreaCodeWithDetails(String areaCode) {
        QTravelCourse c = QTravelCourse.travelCourse;
        QTravelCourseDetailInfo d = QTravelCourseDetailInfo.travelCourseDetailInfo;

        TravelCourse course = queryFactory
            .selectFrom(c)
            .leftJoin(c.detailInfos, d).fetchJoin()
            .where(c.basicInfo.areacode.eq(areaCode))
            .orderBy(c.createdAt.desc())     // 최신 코스 우선
            .limit(1)
            .fetchOne();

        return Optional.ofNullable(course);
    }
}
