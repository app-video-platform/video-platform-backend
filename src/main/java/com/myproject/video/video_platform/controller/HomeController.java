package com.myproject.video.video_platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Hello there! :)";
    }

    @GetMapping("/testEndpoint")
    public String testEndpoint() {
        return "Still now working mate.";
    }
}

