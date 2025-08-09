package com.swyp10.domain.travelcourse.repository;

import com.swyp10.config.QueryDslConfig;
import com.swyp10.config.TestConfig;
import com.swyp10.domain.travelcourse.entity.TravelCourse;
import com.swyp10.domain.travelcourse.entity.TravelCourseBasicInfo;
import com.swyp10.domain.travelcourse.entity.TravelCourseDetailInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestConfig.class, QueryDslConfig.class})
@EntityScan("com.swyp10.domain") // 엔티티 스캔
@ActiveProfiles("test")
class TravelCourseCustomRepositoryImplTest {

    @Autowired
    TravelCourseRepository travelCourseRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("areacode로 최신 TravelCourse 1건 + detailInfos fetch-join 조회 - 성공")
    void findOneByAreaCodeWithDetails_success() {
        // given
        String areacode = "11";

        // 코스 A (먼저 저장)
        TravelCourse courseA = TravelCourse.builder()
            .contentId("111111")
            .basicInfo(TravelCourseBasicInfo.builder()
                .areacode(areacode)
                .title("코스 A")
                .mapx("127.10")
                .mapy("37.50")
                .build())
            .build();

        courseA.addDetailInfo(TravelCourseDetailInfo.builder()
            .subnum("1111113")
            .subcontentid("111111")
            .subname("A-볼거리1")
            .subdetailoverview("A 개요1")
            .subdetailimg("https://a1.jpg")
            .build());

        // 코스 B (나중에 저장 -> createdAt 더 최신)
        TravelCourse courseB = TravelCourse.builder()
            .contentId("222222")
            .basicInfo(TravelCourseBasicInfo.builder()
                .areacode(areacode)
                .title("코스 B")
                .mapx("127.20")
                .mapy("37.60")
                .build())
            .build();

        courseB.addDetailInfo(TravelCourseDetailInfo.builder()
            .subnum("2222223")
            .subcontentid("222222")
            .subname("B-볼거리1")
            .subdetailoverview("B 개요1")
            .subdetailimg("https://b1.jpg")
            .build());
        courseB.addDetailInfo(TravelCourseDetailInfo.builder()
            .subnum("2222224")
            .subcontentid("222222")
            .subname("B-볼거리2")
            .subdetailoverview("B 개요2")
            .subdetailimg("https://b2.jpg")
            .build());

        travelCourseRepository.save(courseA);
        travelCourseRepository.save(courseB);
        em.flush();
        em.clear();

        // when
        var opt = travelCourseRepository.findOneByAreaCodeWithDetails(areacode);

        // then
        assertThat(opt).isPresent();
        TravelCourse found = opt.get();
        // createdAt desc 정렬이므로 B가 나와야 함
        assertThat(found.getContentId()).isEqualTo("222222");
        assertThat(found.getDetailInfos()).hasSize(2); // fetch-join 확인
        assertThat(found.getBasicInfo().getTitle()).isEqualTo("코스 B");
    }

    @Test
    @DisplayName("areacode에 해당하는 코스가 없을 때 Optional.empty 반환 - 성공")
    void findOneByAreaCodeWithDetails_empty() {
        // given
        String area = "99"; // 저장하지 않은 지역

        // when
        var opt = travelCourseRepository.findOneByAreaCodeWithDetails(area);

        // then
        assertThat(opt).isEmpty();
    }
}
