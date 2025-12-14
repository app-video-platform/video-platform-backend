package com.myproject.video.video_platform.dto.products.download;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Download-specific fields nested under product.details (read payload).")
public class DownloadProductDetailsResponseDto {

    @Schema(description = "Ordered sections comprising the download product")
    private List<SectionDownloadProductResponseDto> sections;
}

