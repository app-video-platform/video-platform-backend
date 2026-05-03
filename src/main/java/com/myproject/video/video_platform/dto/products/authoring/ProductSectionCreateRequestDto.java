package com.myproject.video.video_platform.dto.products.authoring;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Canonical request to create a section inside a sectioned product.")
public class ProductSectionCreateRequestDto {

    @NotBlank(message = "Title is required")
    @Schema(description = "Section title", example = "Module 1")
    private String title;

    @Schema(description = "Section description", example = "Introduction to the product content.")
    private String description;

    @Schema(description = "Desired position inside the product", example = "2")
    private Integer position;
}
