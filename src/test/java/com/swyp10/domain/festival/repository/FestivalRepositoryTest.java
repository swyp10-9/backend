package com.swyp10.domain.festival.repository;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.entity.FestivalDetailIntro;
import com.swyp10.domain.festival.entity.FestivalImage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EntityScan(basePackages = "com.swyp10.domain")
@Import({TestConfig.class, FestivalRepositoryTest.QuerydslTestConfig.class})
@ActiveProfiles("test")
class FestivalRepositoryTest {

    @Autowired
    FestivalRepository festivalRepository;

    @TestConfiguration
    static class QuerydslTestConfig {
        @PersistenceContext
        private EntityManager em;

        @Bean
        public com.querydsl.jpa.impl.JPAQueryFactory jpaQueryFactory() {
            return new com.querydsl.jpa.impl.JPAQueryFactory(em);
        }
    }

    @Test
    @DisplayName("findByFestivalId - 연관 정보(EntityGraph)까지 한 번에 조회 성공")
    void findByFestivalId_success() {
        // given
        Festival saved = festivalRepository.save(buildFestivalAggregate("테스트축제"));

        // when
        var found = festivalRepository.findByContentId(saved.getContentId());

        // then
        assertThat(found).isPresent();
        Festival f = found.get();

        // 기본 정보
        assertThat(f.getBasicInfo()).isNotNull();
        assertThat(f.getBasicInfo().getTitle()).isEqualTo("테스트축제");

        // 상세 인트로
        assertThat(f.getDetailIntro()).isNotNull();
        assertThat(f.getDetailIntro().getEventplace()).isEqualTo("테스트장소");

        // 이미지 컬렉션
        assertThat(f.getDetailImages()).hasSize(2);
        assertThat(f.getDetailImages().get(0).getOriginimgurl()).isEqualTo("http://img/1.jpg");
    }

    @Test
    @DisplayName("findByFestivalId - 존재하지 않는 ID 조회 시 empty")
    void findByFestivalId_notFound() {
        // when
        var found = festivalRepository.findByContentId(String.valueOf(999999L));

        // then
        assertThat(found).isEmpty();
    }

    private Festival buildFestivalAggregate(String title) {
        FestivalBasicInfo basic = FestivalBasicInfo.builder()
            .title(title)
            .addr1("서울시 강남구 어딘가")
            .eventstartdate(LocalDate.of(2025, 9, 1))
            .eventenddate(LocalDate.of(2025, 9, 3))
            .firstimage2("http://thumb/xxx.jpg")
            .mapx(127.0123)
            .mapy(37.1234)
            .build();

        FestivalDetailIntro intro = FestivalDetailIntro.builder()
            .sponsor1("스폰서")
            .sponsor1tel("010-0000-0000")
            .eventplace("테스트장소")
            .eventhomepage("<a href='http://example.com'>홈</a>")
            .playtime("09:00~18:00")
            .usetimefestival("무료")
            .build();

        List<FestivalImage> images = List.of(
            FestivalImage.builder()
                .imgid("1111").originimgurl("http://img/1.jpg").smallimageurl("http://img/1_s.jpg").build(),
            FestivalImage.builder()
                .imgid("2222").originimgurl("http://img/2.jpg").smallimageurl("http://img/2_s.jpg").build()
        );

        return Festival.builder()
            .contentId("1234")
            .overview("개요 텍스트")
            .basicInfo(basic)
            .detailIntro(intro)
            .detailImages(images)
            .build();
    }
}
