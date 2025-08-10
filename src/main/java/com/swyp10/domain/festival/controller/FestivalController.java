package com.swyp10.domain.festival.controller;

import com.swyp10.config.security.OptionalUserId;
import com.swyp10.domain.bookmark.service.UserBookmarkService;
import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.service.FestivalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
@Tag(name = "축제", description = "축제 조회 및 북마크 API")
public class FestivalController {

    private final FestivalService festivalService;
    private final UserBookmarkService bookmarkService;

    @Operation(summary = "축제 리스트 조회 - 지도 페이지", description = "축제 리스트 조회 - 지도 페이지")
    @GetMapping("/map")
    public FestivalListResponse getFestivalsForMap(@ModelAttribute @ParameterObject FestivalMapRequest request) {
        return festivalService.getFestivalsForMap(request);
    }

    @Operation(summary = "축제 리스트 조회 - 달력 페이지", description = "축제 리스트 조회 - 달력 페이지")
    @GetMapping("/calendar")
    public FestivalListResponse getFestivalsForCalendar(@ModelAttribute @ParameterObject FestivalCalendarRequest request) {
        return festivalService.getFestivalsForCalendar(request);
    }

    @Operation(summary = "축제 리스트 조회 - 맞춤 축제 페이지", description = "축제 리스트 조회 - 맞춤 축제 페이지")
    @GetMapping("/personal-test")
    public FestivalListResponse getFestivalsForPersonalTest(@ModelAttribute @ParameterObject FestivalPersonalTestRequest request) {
        return festivalService.getFestivalsForPersonalTest(request);
    }

    @Operation(summary = "축제 리스트 조회 - 검색 페이지", description = "축제 리스트 조회 - 검색 페이지")
    @GetMapping("/search")
    public FestivalListResponse searchFestivals(@ModelAttribute @ParameterObject FestivalSearchRequest request) {
        return festivalService.searchFestivals(request);
    }

    @Operation(
        summary = "축제 리스트 조회 - 마이페이지",
        description = "내 북마크한 축제 목록(페이징)",
        security = { @SecurityRequirement(name = "Bearer Authentication") }
    )
    @GetMapping("/mypage")
    public FestivalListResponse getMyPageFestivals(
        @Parameter(hidden = true) @OptionalUserId Long userId,  // Swagger에서 숨김
        @ModelAttribute @ParameterObject FestivalMyPageRequest request
    ) {
        return festivalService.getMyBookmarkedFestivals(userId, request);
    }

    // =========================== 북마크 API ===========================

    @Operation(
        summary = "북마크 저장",
        description = "로그인 사용자의 축제 북마크 저장",
        security = { @SecurityRequirement(name = "Bearer Authentication") }
    )
    @PostMapping("/{festivalId}/bookmarks")
    public Long addBookmark(
        @PathVariable Long festivalId,
        @Parameter(hidden = true) @OptionalUserId Long userId
    ) {
        return bookmarkService.addBookmark(userId, festivalId);
    }

    @Operation(
        summary = "북마크 삭제",
        description = "로그인 사용자의 축제 북마크 삭제",
        security = { @SecurityRequirement(name = "Bearer Authentication") }
    )
    @DeleteMapping("/{festivalId}/bookmarks")
    public void removeBookmark(
        @PathVariable Long festivalId,
        @Parameter(hidden = true) @OptionalUserId Long userId
    ) {
        bookmarkService.removeBookmark(userId, festivalId);
    }

    @Operation(
        summary = "북마크 상태 확인",
        description = "특정 축제의 북마크 상태 확인",
        security = { @SecurityRequirement(name = "Bearer Authentication") }
    )
    @GetMapping("/{festivalId}/bookmarks/status")
    public boolean getBookmarkStatus(
        @PathVariable Long festivalId,
        @Parameter(hidden = true) @OptionalUserId Long userId
    ) {
        return bookmarkService.isBookmarked(userId, festivalId);
    }
}
