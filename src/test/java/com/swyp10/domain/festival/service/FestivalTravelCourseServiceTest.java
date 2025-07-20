package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.entity.FestivalTravelCourse;
import com.swyp10.domain.festival.entity.FestivalTravelCourseId;
import com.swyp10.domain.festival.repository.FestivalTravelCourseRepository;
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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FestivalTravelCourseService 테스트")
class FestivalTravelCourseServiceTest {

    @Mock
    private FestivalTravelCourseRepository repository;

    @InjectMocks
    private FestivalTravelCourseService service;

    private FestivalTravelCourse testMapping;

    @BeforeEach
    void setUp() {
        FestivalTravelCourseId id = new FestivalTravelCourseId(1L, 1L);
        testMapping = FestivalTravelCourse.builder()
            .id(id)
            .build();
    }

    @Nested
    @DisplayName("조회 테스트")
    class GetTests {

        @Test
        @DisplayName("정상 조회")
        void getByFestivalId_success() {
            given(repository.findByFestival_Id(1L))
                .willReturn(Collections.singletonList(testMapping));

            List<FestivalTravelCourse> result = service.getByFestivalId(1L);

            assertThat(result).hasSize(1);
            verify(repository).findByFestival_Id(1L);
        }

        @Test
        @DisplayName("잘못된 festivalId로 조회 시 예외 발생")
        void getByFestivalId_invalid() {
            assertThatThrownBy(() -> service.getByFestivalId(0L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("매핑 추가 테스트")
    class AddTests {

        @Test
        @DisplayName("정상 추가")
        void addMapping_success() {
            given(repository.save(testMapping)).willReturn(testMapping);

            FestivalTravelCourse result = service.addMapping(testMapping);

            assertThat(result).isEqualTo(testMapping);
            verify(repository).save(testMapping);
        }

        @Test
        @DisplayName("null 입력 시 예외")
        void addMapping_null() {
            assertThatThrownBy(() -> service.addMapping(null))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("매핑 삭제 테스트")
    class DeleteTests {

        @Test
        @DisplayName("정상 삭제")
        void removeMapping_success() {
            service.removeMapping(1L, 1L);

            verify(repository).deleteByFestival_IdAndTravelCourse_Id(1L, 1L);
        }

        @Test
        @DisplayName("잘못된 festivalId 입력 시 예외")
        void removeMapping_invalidFestivalId() {
            assertThatThrownBy(() -> service.removeMapping(0L, 1L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }

        @Test
        @DisplayName("잘못된 courseId 입력 시 예외")
        void removeMapping_invalidCourseId() {
            assertThatThrownBy(() -> service.removeMapping(1L, -1L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }
}
