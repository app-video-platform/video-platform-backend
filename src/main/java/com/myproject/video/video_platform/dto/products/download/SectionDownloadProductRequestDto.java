package com.myproject.video.video_platform.dto.products.download;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Draft state for a downloadable section grouping files.")
public class SectionDownloadProductRequestDto {
    @Schema(description = "Section identifier when updating", example = "c5c4a1d0-8d61-4ff0-9dbe-2a4d5e3da5b1")
    private String id;
    @Schema(description = "Section title displayed to learners", example = "Preset Pack & Shooting Checklist")
    private String title;
    @Schema(description = "Optional teaser text", example = "Download the RAW presets and printable checklist used in module two.")
    private String description;
    @Schema(description = "Relative position (starting at 1)", example = "1")
    private Integer position;
}
