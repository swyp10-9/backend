package com.swyp10.domain.bookmark.controller;

import com.swyp10.domain.bookmark.service.UserBookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
@Tag(name = "북마크", description = "북마크 저장 API")
public class FestivalBookmarkController {

    private final UserBookmarkService bookmarkService;

    @Operation(summary = "북마크 저장",
        description = "로그인 사용자의 축제 북마크 저장",
        security = { @SecurityRequirement(name = "Bearer Authentication") }
    )
    @PostMapping("/{festivalId}/bookmarks")
    public Long addBookmark(
        @PathVariable Long festivalId,
        @AuthenticationPrincipal Long userId
    ) {
        return bookmarkService.addBookmark(userId, festivalId);
    }
}
