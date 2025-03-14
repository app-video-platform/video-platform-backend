package com.myproject.video.video_platform.dto.products_creation;

import lombok.Data;

import java.util.UUID;

@Data
public class SectionDownloadProductResponseDto {
    private UUID id;
    private String title;
    private String description;
    private Integer position;
}
