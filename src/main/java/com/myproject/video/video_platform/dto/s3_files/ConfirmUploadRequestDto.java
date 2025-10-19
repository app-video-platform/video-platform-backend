package com.myproject.video.video_platform.dto.s3_files;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Payload confirming a direct Spaces upload for downloadable assets.")
public class ConfirmUploadRequestDto {
    @Schema(description = "Target section identifier", example = "c5c4a1d0-8d61-4ff0-9dbe-2a4d5e3da5b1")
    private String sectionId;
    @Schema(description = "Spaces object key returned when creating the presigned URL", example = "users-content/738297f1-45fb-4f5f-98a5-6d0eb0a8f542/download_section_files/c5c4a1d0-8d61-4ff0-9dbe-2a4d5e3da5b1/0a4b3c22-1cb9-41d6-9e2c-7c1b4e2862fa_lifestyle-preset-pack.zip")
    private String key;
    @Schema(description = "Public CDN URL if computed client-side", example = "https://video-platform.fra1.cdn.digitaloceanspaces.com/users-content/...")
    private String fileUrl;
    @Schema(description = "Original filename", example = "lifestyle-preset-pack.zip")
    private String fileName;
    @Schema(description = "File size in bytes", example = "48234123")
    private Long fileSize;
    @Schema(description = "MIME type", example = "application/zip")
    private String fileType;
}
