package com.myproject.video.video_platform.dto.creator.dashboard;

import java.util.List;

public final class CreatorDashboardDtos {
    private CreatorDashboardDtos() {}

    public record Metric(String id, String label, String value, String comparison, String direction,
                         String sentiment, String state, String destinationPath) {}
    public record Activity(String id, String kind, String title, String context, String value, String status,
                           String timestamp, String destinationPath) {}
    public record TopProduct(String id, String name, String type, String revenue, double revenueShare,
                             String thumbnailUrl, String destinationPath) {}
    public record AttentionItem(String id, String issue, String context, String severity, String actionLabel,
                                String actionPath, String actionDisabledReason) {}
    public record Summary(List<Metric> metrics, List<Activity> activities, List<TopProduct> topProducts,
                          List<AttentionItem> attentionItems) {}
}
