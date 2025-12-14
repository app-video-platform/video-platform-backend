package com.myproject.video.video_platform.dto.products.download;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Download-specific fields nested under product.details (write payload).")
public class DownloadProductDetailsRequestDto {

    @Schema(description = "Ordered sections within the download product")
    private List<SectionDownloadProductRequestDto> sections;
}

