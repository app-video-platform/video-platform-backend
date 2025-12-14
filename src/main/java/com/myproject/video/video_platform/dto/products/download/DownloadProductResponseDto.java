package com.myproject.video.video_platform.dto.products.download;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("DOWNLOAD")
@Schema(description = "Download product view with details nested under product.details.")
public class DownloadProductResponseDto extends AbstractProductResponseDto {

    @Schema(description = "Download-specific fields nested under product.details")
    private DownloadProductDetailsResponseDto details;
}
