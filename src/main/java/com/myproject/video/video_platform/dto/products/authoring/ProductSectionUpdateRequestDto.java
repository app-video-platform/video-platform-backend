package com.myproject.video.video_platform.dto.products.authoring;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Canonical request to patch a section inside a sectioned product.")
public class ProductSectionUpdateRequestDto {

    @Schema(description = "Section title", example = "Module 1")
    private String title;

    @Schema(description = "Section description", example = "Introduction to the product content.")
    private String description;

    @Schema(description = "Desired position inside the product", example = "2")
    private Integer position;
}
