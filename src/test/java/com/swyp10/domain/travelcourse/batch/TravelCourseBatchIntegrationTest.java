package com.swyp10.domain.travelcourse.batch;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.travelcourse.entity.TravelCourse;
import com.swyp10.domain.travelcourse.repository.TravelCourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestConfig.class, TravelCourseBatchTestConfig.class})
@ActiveProfiles("test")
@DisplayName("TravelCourseBatch 통합 테스트")
public class TravelCourseBatchIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job travelCourseSyncJob;

    @Autowired
    private TravelCourseRepository travelCourseRepository;

    @Test
    @DisplayName("TravelCourseBatchConfig 통합 테스트 - 잡 실행 및 데이터 저장 검증")
    void testTravelCourseBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(travelCourseSyncJob, jobParameters);

        // 잡이 정상 종료되었는지 확인
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // DB에 여행코스 데이터가 최소 1건 이상 저장되었는지 확인
        List<TravelCourse> travelCourses = travelCourseRepository.findAll();
        assertThat(travelCourses).isNotEmpty();

        // 저장된 첫 번째 데이터 필드 검증 (예: contentId, 제목)
        TravelCourse first = travelCourses.get(0);
        assertThat(first.getContentId()).isNotNull();
        assertThat(first.getBasicInfo().getTitle()).isNotNull();
    }
}
