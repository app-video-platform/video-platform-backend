package com.myproject.video.video_platform.dto.products_creation.download;

import lombok.Data;

import java.util.UUID;

@Data
public class FileDownloadProductResponseDto {
    private UUID id;
    private String fileName;
    private long   size;
    private String fileType;
    private String url;       // full CDN URL
}
