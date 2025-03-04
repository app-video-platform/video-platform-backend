package com.myproject.video.video_platform.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Hello there! :)";
    }

    @GetMapping("/testEndpoint")
    public String testEndpoint() {
        log.info("testEndpoint");
        return "Still now working mate.";
    }
}

