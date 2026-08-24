package com.myproject.video.video_platform.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ReportingClockConfig {

    @Bean
    public Clock reportingClock() {
        return Clock.systemUTC();
    }
}
