package com.myproject.video.video_platform.dto.products.download;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("DOWNLOAD")
@Schema(description = "Payload for creating or updating a download bundle product.")
public class DownloadProductRequestDto extends AbstractProductRequestDto {
    @Schema(description = "Ordered sections within the download product")
    private List<SectionDownloadProductRequestDto> sections;
}
