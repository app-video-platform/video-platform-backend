package com.myproject.video.video_platform.dto.products.download;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Section containing downloadable files." )
public class SectionDownloadProductResponseDto {
    @Schema(description = "Section identifier", example = "c5c4a1d0-8d61-4ff0-9dbe-2a4d5e3da5b1")
    private UUID id;
    @Schema(description = "Section title", example = "Preset Pack & Shooting Checklist")
    private String title;
    @Schema(description = "Optional summary shown to learners", example = "Download the RAW presets and printable checklist used in module two.")
    private String description;
    @Schema(description = "Relative ordering (starting at 1)", example = "1")
    private Integer position;
    @Schema(description = "Files available inside the section")
    private List<FileDownloadProductResponseDto> files;
}
