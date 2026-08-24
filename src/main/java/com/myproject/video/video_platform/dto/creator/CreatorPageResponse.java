package com.myproject.video.video_platform.dto.creator;

import java.util.List;

public record CreatorPageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int size,
        int number,
        boolean first,
        boolean last,
        boolean empty
) {
    public static <T> CreatorPageResponse<T> of(List<T> all, int page, int size) {
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        int totalPages = all.isEmpty() ? 0 : (int) Math.ceil((double) all.size() / size);
        return new CreatorPageResponse<>(
                List.copyOf(all.subList(from, to)), all.size(), totalPages, size, page,
                page == 0, totalPages == 0 || page >= totalPages - 1, from == to
        );
    }
}
