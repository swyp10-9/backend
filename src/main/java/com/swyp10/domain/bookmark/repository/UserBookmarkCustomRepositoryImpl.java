package com.swyp10.domain.bookmark.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp10.domain.bookmark.entity.QUserBookmark;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.QFestival;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserBookmarkCustomRepositoryImpl implements UserBookmarkCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Set<Long> findBookmarkedFestivalIds(Long userId, Collection<Long> festivalIds) {
        QUserBookmark ub = QUserBookmark.userBookmark;

        if (festivalIds == null || festivalIds.isEmpty()) {
            return java.util.Collections.emptySet();
        }

        return queryFactory
            .select(ub.festival.festivalId)
            .from(ub)
            .where(
                ub.user.userId.eq(userId),
                ub.festival.festivalId.in(festivalIds),
                ub.deletedAt.isNull()
            )
            .fetch()
            .stream()
            .collect(Collectors.toSet());
    }

    @Override
    public Page<FestivalSummaryResponse> findBookmarkedFestivals(Long userId, Pageable pageable) {
        QUserBookmark ub = QUserBookmark.userBookmark;
        QFestival f = QFestival.festival;

        BooleanBuilder where = new BooleanBuilder()
            .and(ub.user.userId.eq(userId))
            .and(ub.deletedAt.isNull());

        // content
        List<Festival> content = queryFactory
            .select(f)
            .from(ub)
            .join(ub.festival, f)
            .where(where)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(ub.createdAt.desc())
            .fetch();

        // total
        long total = queryFactory
            .select(ub.count())
            .from(ub)
            .where(where)
            .fetchOne();

        List<FestivalSummaryResponse> dtos = content.stream()
            .map(FestivalSummaryResponse::from)
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, total);
    }
}