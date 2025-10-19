package com.myproject.video.video_platform.dto.products.download;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("DOWNLOAD")
@Schema(description = "Download product view including nested file sections.")
public class DownloadProductResponseDto extends AbstractProductResponseDto {
    @Schema(description = "Ordered sections comprising the download product")
    private List<SectionDownloadProductResponseDto> sections;
}
