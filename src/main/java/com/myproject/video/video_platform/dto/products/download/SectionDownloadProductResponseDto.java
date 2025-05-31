package com.myproject.video.video_platform.dto.products.download;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SectionDownloadProductResponseDto {
    private UUID id;
    private String title;
    private String description;
    private Integer position;
    private List<FileDownloadProductResponseDto> files;
}
