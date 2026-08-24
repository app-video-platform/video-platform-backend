package com.myproject.video.video_platform.controller.creator;

import com.myproject.video.video_platform.controller.docs.creator.CreatorDashboardApiDoc;
import com.myproject.video.video_platform.dto.creator.dashboard.CreatorDashboardDtos;
import com.myproject.video.video_platform.service.creator.CreatorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/creator/dashboard")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
public class CreatorDashboardController implements CreatorDashboardApiDoc {
    private final CreatorDashboardService service;

    @Override @GetMapping("/summary")
    public ResponseEntity<CreatorDashboardDtos.Summary> summary() {
        return ResponseEntity.ok(service.summary());
    }
}
