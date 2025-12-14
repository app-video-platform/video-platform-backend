package com.myproject.video.video_platform.controller;

import com.myproject.video.video_platform.controller.docs.system.SystemApiDoc;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Tag(name = "System", description = "Utility endpoints for health checks and diagnostics.")
public class HomeController implements SystemApiDoc {

    @GetMapping("/")
    @Override
    public String home() {
        return "Hello there! :)";
    }

    @GetMapping("/testEndpoint")
    @Override
    public String testEndpoint() {
        log.info("testEndpoint");
        return "Still now working mate.";
    }
}
