package com.myproject.video.video_platform.dto.products.media;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class GalleryOrderRequest {
    private List<UUID> imageIds;
}
