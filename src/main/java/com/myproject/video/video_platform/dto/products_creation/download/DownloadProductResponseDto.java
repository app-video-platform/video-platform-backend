package com.myproject.video.video_platform.dto.products_creation.download;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("DOWNLOAD")
public class DownloadProductResponseDto extends AbstractProductResponseDto {
    private List<SectionDownloadProductResponseDto> sections;
}
