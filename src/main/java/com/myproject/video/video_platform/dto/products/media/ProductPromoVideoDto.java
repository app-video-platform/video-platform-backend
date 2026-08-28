package com.myproject.video.video_platform.dto.products.media;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProductPromoVideoDto {
    private UUID id;
    private String url;
    private String fileName;
    private String fileType;
    private long size;
    private String status;
    private String thumbnailUrl;
}
