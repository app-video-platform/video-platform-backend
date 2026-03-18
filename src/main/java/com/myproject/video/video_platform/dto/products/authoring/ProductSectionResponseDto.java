package com.myproject.video.video_platform.dto.products.authoring;

import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.dto.products.download.FileDownloadProductResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Canonical section response used by sectioned product authoring endpoints.")
public class ProductSectionResponseDto {

    @Schema(description = "Section identifier", example = "c84b79a6-9d4d-4bb4-8f3c-3eb8bb76390a")
    private String id;

    @Schema(description = "Owning product identifier", example = "a8c5d4a9-dc93-4c71-9c33-5d56f3d6b21d")
    private String productId;

    @Schema(description = "Section title", example = "Module 1")
    private String title;

    @Schema(description = "Section description", example = "Introduction to the product content.")
    private String description;

    @Schema(description = "Relative ordering", example = "1")
    private Integer position;

    @Schema(description = "Course lessons when the product type is COURSE")
    private List<CourseLessonResponseDto> lessons;

    @Schema(description = "Download files when the product type is DOWNLOAD")
    private List<FileDownloadProductResponseDto> files;
}
