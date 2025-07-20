package com.swyp10.domain.travelcourse.service;

import com.swyp10.domain.travelcourse.entity.TravelCourse;
import com.swyp10.domain.travelcourse.entity.TravelDifficulty;
import com.swyp10.domain.travelcourse.repository.TravelCourseRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TravelCourseService 테스트")
class TravelCourseServiceTest {

    @Mock
    private TravelCourseRepository travelCourseRepository;

    @InjectMocks
    private TravelCourseService travelCourseService;

    private TravelCourse course;

    @BeforeEach
    void setUp() {
        course = TravelCourse.builder()
            .id(1L)
            .title("부산 해운대 코스")
            .durationHours(3)
            .difficultyLevel(TravelDifficulty.EASY)
            .build();
    }

    @Nested
    @DisplayName("여행 코스 조회")
    class GetCourse {

        @Test
        @DisplayName("ID로 코스 조회 성공")
        void getByIdSuccess() {
            // given
            given(travelCourseRepository.findById(1L)).willReturn(Optional.of(course));

            // when
            TravelCourse result = travelCourseService.getTravelCourse(1L);

            // then
            assertThat(result).isEqualTo(course);
            verify(travelCourseRepository).findById(1L);
        }

        @Test
        @DisplayName("ID로 코스 조회 실패 시 예외 발생")
        void getByIdFail() {
            // given
            given(travelCourseRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> travelCourseService.getTravelCourse(99L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("여행 코스 등록")
    class CreateCourse {

        @Test
        @DisplayName("코스 등록 성공")
        void create() {
            // given
            given(travelCourseRepository.save(course)).willReturn(course);

            // when
            TravelCourse result = travelCourseService.createTravelCourse(course);

            // then
            assertThat(result).isEqualTo(course);
            verify(travelCourseRepository).save(course);
        }
    }

    @Nested
    @DisplayName("여행 코스 삭제")
    class DeleteCourse {

        @Test
        @DisplayName("코스 삭제 성공")
        void delete() {
            // when
            travelCourseService.deleteTravelCourse(1L);

            // then
            verify(travelCourseRepository).deleteById(1L);
        }
    }
}