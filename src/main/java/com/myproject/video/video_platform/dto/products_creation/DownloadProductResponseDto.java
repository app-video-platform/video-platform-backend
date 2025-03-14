package com.myproject.video.video_platform.dto.products_creation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("DOWNLOAD")
public class DownloadProductResponseDto extends AbstractProductResponseDto {
    private List<SectionDownloadProductResponseDto> sections;
}
