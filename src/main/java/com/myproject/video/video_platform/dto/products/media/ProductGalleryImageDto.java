package com.myproject.video.video_platform.dto.products.media;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProductGalleryImageDto {
    private UUID id;
    private String url;
    private String fileName;
    private String fileType;
    private long size;
    private int position;
    private String altText;
    private String status;
}
