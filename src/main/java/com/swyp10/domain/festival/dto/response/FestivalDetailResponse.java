package com.swyp10.domain.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FestivalDetailResponse {
    @Schema(description = "축제 ID", example = "1001")
    private Long id;
    @Schema(description = "축제명", example = "부산 불꽃축제")
    private String title;
    @Schema(description = "주소", example = "부산광역시 해운대구")
    private String address;
    @Schema(description = "테마", example = "음식/미식")
    private String theme;
    @Schema(description = "축제 시작일", example = "2025-09-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String startDate;
    @Schema(description = "축제 종료일", example = "2025-09-03")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String endDate;
    @Schema(description = "썸네일 이미지 URL", example = "https://...")
    private String thumbnail;
    @Schema(description = "경도", example = "127.5881015063")
    private String mapx;
    @Schema(description = "위도", example = "36.9913818048")
    private String mapy;
    @Schema(description = "이미지 목록")
    private List<ImageResponse> images;
    @Schema(description = "상세 내용")
    private ContentResponse content;
    @Schema(description = "추가 정보")
    private InfoResponse info;

    @Getter
    @Builder
    public static class ImageResponse {
        @Schema(description = "콘텐츠ID", example = "142228")
        private String contentid;
        @Schema(description = "원본 이미지 URL", example = "http://tong.visitkorea.or.kr/cms/resource/23/3493223_image2_1.JPG")
        private String originimgurl;
        @Schema(description = "이미지명", example = "2025 태백 해바라기축제 5")
        private String imgname;
        @Schema(description = "작은 이미지 URL", example = "http://tong.visitkorea.or.kr/cms/resource/23/3493223_image3_1.JPG")
        private String smallimageurl;
    }

    @Getter
    @Builder
    public static class ContentResponse {
        @Schema(description = "축제 타이틀", example = "태백 해바라기축제")
        private String title;
        @Schema(description = "홈페이지 HTML", example = "<a href=\"...\">http://sunflowerfestival.co.kr/</a>")
        private String homepage;
        @Schema(description = "주소1", example = "강원특별자치도 태백시 구와우길 38-20")
        private String addr1;
        @Schema(description = "주소2", example = "(황지동)")
        private String addr2;
        @Schema(description = "개요/소개", example = "태백해바라기 축제는 ...")
        private String overview;
    }

    @Getter
    @Builder
    public static class InfoResponse {
        @Schema(description = "주최", example = "해바라기문화재단")
        private String sponsor1;
        @Schema(description = "주최 연락처", example = "010-3371-2000")
        private String sponsor1tel;
        @Schema(description = "행사 시작일(YYYYMMDD)", example = "20250718")
        private String eventstartdate;
        @Schema(description = "행사 종료일(YYYYMMDD)", example = "20250817")
        private String eventenddate;
        @Schema(description = "운영 시간", example = "07:00 ~ 18:00")
        private String playtime;
        @Schema(description = "장소", example = "태백해바라기축제장")
        private String eventplace;
        @Schema(description = "행사 홈페이지", example = "")
        private String eventhomepage;
        @Schema(description = "이용 요금", example = "유료 <br>일반 : 7천원  / 학생(초중고) : 5천원 <br>단체(20이상) : 5천원")
        private String usetimefestival;
        @Schema(description = "할인 정보", example = "")
        private String discountinfofestival;
        @Schema(description = "소요 시간", example = "")
        private String spendtimefestival;
    }
}
