package com.swyp10.domain.festival.controller;

import com.swyp10.domain.festival.dto.response.FestivalDetailResponse;
import com.swyp10.domain.festival.service.FestivalDetailService;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FestivalDetailController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FestivalDetailController 테스트")
class FestivalDetailControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FestivalDetailService festivalService;

    @Test
    @DisplayName("축제 상세 조회 - 성공")
    void getFestivalDetail_success() throws Exception {
        // given
        FestivalDetailResponse mockResponse = FestivalDetailResponse.builder()
            .startDate("2025-09-25")
            .endDate("2025-09-28")
            .thumbnail("http://tong.visitkorea.or.kr/cms/resource/81/3338681_image2_1.jpg")
            .mapx("127.5881015063")
            .mapy("36.9913818048")
            .title("음성명작페스티벌")
            .images(List.of())
            .content(null)
            .info(null)
            .build();

        when(festivalService.getFestivalDetail(anyLong()))
            .thenReturn(mockResponse);

        // expected
        mockMvc.perform(get("/api/v1/festivals/1001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("음성명작페스티벌"))
            .andExpect(jsonPath("$.data.startDate").value("2025-09-25"))
            .andExpect(jsonPath("$.data.endDate").value("2025-09-28"))
            .andExpect(jsonPath("$.data.thumbnail").value("http://tong.visitkorea.or.kr/cms/resource/81/3338681_image2_1.jpg"))
            .andExpect(jsonPath("$.data.mapx").value("127.5881015063"))
            .andExpect(jsonPath("$.data.mapy").value("36.9913818048"));
    }

    @Test
    @DisplayName("존재하지 않는 축제 상세 조회시 404 반환 - 실패")
    void getFestivalDetail_notFound() throws Exception {
        // given
        when(festivalService.getFestivalDetail(anyLong()))
            .thenThrow(new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND, "축제를 찾을 수 없습니다."));

        // expected
        mockMvc.perform(get("/api/v1/festivals/9999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("축제를 찾을 수 없습니다."));
    }

}
