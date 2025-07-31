package com.swyp10.domain.travelcourse.controller;

import com.swyp10.domain.travelcourse.dto.request.FestivalTravelCoursePageRequest;
import com.swyp10.domain.travelcourse.dto.response.FestivalTravelCourseListResponse;
import com.swyp10.domain.travelcourse.dto.response.FestivalTravelCourseResponse;
import com.swyp10.domain.travelcourse.service.TravelCourseService;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FestivalTravelCourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FestivalTravelCourseController 테스트")
class FestivalTravelCourseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TravelCourseService travelCourseService;

    @Nested
    @DisplayName("여행 코스 조회 API")
    class GetFestivalTravelCourses {

        @Test
        @DisplayName("여행 코스 리스트 반환 - 성공")
        void getFestivalTravelCourses_success() throws Exception {
            // given
            FestivalTravelCourseListResponse mockResponse = FestivalTravelCourseListResponse.builder()
                .courses(List.of(
                    FestivalTravelCourseResponse.builder()
                        .id(1L)
                        .title("해운대 산책로")
                        .time("11:00")
                        .build()
                ))
                .build();

            when(travelCourseService.getFestivalTravelCourses(any(FestivalTravelCoursePageRequest.class)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/festivals/{festivalId}/travel-courses", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses[0].id").value(1L))
                .andExpect(jsonPath("$.data.courses[0].title").value("해운대 산책로"));
        }

        @Test
        @DisplayName("축제가 존재하지 않는 경우 404 반환 - 실패")
        void getFestivalTravelCourses_notFound() throws Exception {
            // given
            when(travelCourseService.getFestivalTravelCourses(any(FestivalTravelCoursePageRequest.class)))
                .thenThrow(new com.swyp10.exception.ApplicationException(
                    ErrorCode.FESTIVAL_NOT_FOUND, "축제를 찾을 수 없습니다."
                ));

            // when & then
            mockMvc.perform(get("/api/v1/festivals/{festivalId}/travel-courses", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("축제를 찾을 수 없습니다."));
        }
    }
}
