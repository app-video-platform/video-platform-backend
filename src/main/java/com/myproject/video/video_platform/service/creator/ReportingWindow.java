package com.myproject.video.video_platform.service.creator;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public record ReportingWindow(Instant start, Instant end, Instant previousStart, Instant previousEnd) {
    public static ReportingWindow forPeriod(ReportingEnums.Period period, Clock clock) {
        LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        Instant end = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant start = end.minusSeconds(period.days() * 86_400L);
        Instant previousStart = start.minusSeconds(period.days() * 86_400L);
        return new ReportingWindow(start, end, previousStart, start);
    }

    public boolean current(Instant instant) {
        return instant != null && !instant.isBefore(start) && instant.isBefore(end);
    }

    public boolean previous(Instant instant) {
        return instant != null && !instant.isBefore(previousStart) && instant.isBefore(previousEnd);
    }
}
