package com.swyp10.domain.festival.service;

import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
import com.swyp10.domain.festival.dto.response.FestivalDetailResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.entity.FestivalDetailIntro;
import com.swyp10.domain.festival.entity.FestivalImage;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalDetailService {

    private final FestivalRepository festivalRepository;
    private final UserBookmarkRepository userBookmarkRepository;

    /**
     * 축제 상세 조회 (북마크 상태 포함)
     */
    public FestivalDetailResponse getFestivalDetail(Long festivalId, Long userId) {
        System.out.println("=== 축제 상세 조회 시작 ===");
        System.out.println("festivalId: " + festivalId + ", userId: " + userId);
        
        // festival_id(PK)로 축제 찾기
        Festival festival = festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND, "축제를 찾을 수 없습니다. id=" + festivalId));
        
        // 북마크 상태 확인
        boolean isBookmarked = false;
        if (userId != null) {
            isBookmarked = userBookmarkRepository.existsByUser_UserIdAndFestival_FestivalIdAndDeletedAtIsNull(userId, festivalId);
            System.out.println("북마크 상태 확인: userId=" + userId + ", festivalId=" + festivalId + ", isBookmarked=" + isBookmarked);
        } else {
            System.out.println("로그인 안됨, bookmarked=false");
        }
        
        return toDetailResponse(festival, isBookmarked);
    }

    private FestivalDetailResponse toDetailResponse(Festival festival, boolean isBookmarked) {
        FestivalBasicInfo basic = festival.getBasicInfo();
        FestivalDetailIntro intro = festival.getDetailIntro();

        // startDate/endDate는 상세의 "yyyy-MM-dd" String으로 노출
        DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String startDateStr = (basic != null && basic.getEventstartdate() != null)
            ? basic.getEventstartdate().format(yyyy_MM_dd) : null;
        String endDateStr = (basic != null && basic.getEventenddate() != null)
            ? basic.getEventenddate().format(yyyy_MM_dd) : null;

        // theme는 Enum → displayName 또는 코드값 등 프로젝트 규칙에 맞춰서
        String theme = festival.getTheme() != null ? festival.getTheme().getDisplayName() : null;

        // 썸네일/좌표
        String thumbnail = (basic != null) ? safeToString(basic.getFirstimage2()) : null;
        String mapx = (basic != null && basic.getMapx() != null) ? String.valueOf(basic.getMapx()) : null;
        String mapy = (basic != null && basic.getMapy() != null) ? String.valueOf(basic.getMapy()) : null;

        // 이미지 목록
        List<FestivalDetailResponse.ImageResponse> images = festival.getDetailImages() == null ? List.of()
            : festival.getDetailImages().stream()
            .map(this::toImageResponse)
            .toList();

        // content 블록
        FestivalDetailResponse.ContentResponse content = FestivalDetailResponse.ContentResponse.builder()
            .title(basic != null ? basic.getTitle() : null)
            .homepage(intro != null ? intro.getEventhomepage() : null)
            .addr1(basic != null ? basic.getAddr1() : null)
            .addr2(basic != null ? basic.getAddr1() : null)
            .overview(festival.getOverview())
            .build();

        // info 블록 (DetailIntro 중심, 값이 없으면 null 허용)
        FestivalDetailResponse.InfoResponse info = FestivalDetailResponse.InfoResponse.builder()
            .sponsor1(intro != null ? intro.getSponsor1() : null)
            .sponsor1tel(intro != null ? intro.getSponsor1tel() : null)
            .eventstartdate(basic != null && basic.getEventstartdate() != null ? basic.getEventstartdate().format(yyyyMMdd) : null)
            .eventenddate(basic != null && basic.getEventenddate() != null ? basic.getEventenddate().format(yyyyMMdd) : null)
            .playtime(intro != null ? intro.getPlaytime() : null)
            .eventplace(intro != null ? intro.getEventplace() : null)
            .eventhomepage(intro != null ? intro.getEventhomepage() : null)
            .usetimefestival(intro != null ? intro.getUsetimefestival() : null)
            .discountinfofestival(intro != null ? intro.getDiscountinfofestival() : null)
            .spendtimefestival(intro != null ? intro.getSpendtimefestival() : null)
            .build();

        return FestivalDetailResponse.builder()
            .id(festival.getFestivalId()) // festival_id(PK) 사용
            .title(basic != null ? basic.getTitle() : null)
            .address(basic != null ? basic.getAddr1() : null)
            .theme(theme)
            .startDate(startDateStr)
            .endDate(endDateStr)
            .thumbnail(thumbnail)
            .mapx(mapx)
            .mapy(mapy)
            .images(images)
            .content(content)
            .info(info)
            .bookmarked(isBookmarked)  // 북마크 상태 추가
            .build();
    }

    private FestivalDetailResponse.ImageResponse toImageResponse(FestivalImage img) {
        return FestivalDetailResponse.ImageResponse.builder()
            .contentid(img.getImgid())
            .originimgurl(img.getOriginimgurl())
            .smallimageurl(img.getSmallimageurl())
            .build();
    }

    private String safeToString(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
