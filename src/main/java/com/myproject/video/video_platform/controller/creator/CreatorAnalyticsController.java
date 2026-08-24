package com.myproject.video.video_platform.controller.creator;

import com.myproject.video.video_platform.controller.docs.creator.CreatorAnalyticsApiDoc;
import com.myproject.video.video_platform.dto.creator.analytics.CreatorAnalyticsDtos;
import com.myproject.video.video_platform.service.creator.CreatorAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/creator/analytics")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
public class CreatorAnalyticsController implements CreatorAnalyticsApiDoc {
    private final CreatorAnalyticsService analyticsService;

    @Override
    @GetMapping("/overview")
    public ResponseEntity<CreatorAnalyticsDtos.Overview> overview(
            @RequestParam(defaultValue = "30d") String period
    ) {
        return ResponseEntity.ok(analyticsService.overview(period));
    }
}
