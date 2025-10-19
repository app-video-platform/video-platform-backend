package com.myproject.video.video_platform.dto.s3_files;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Confirmation payload after persisting a Spaces upload reference.")
public class FileS3UploadResponseDto {
    @Schema(description = "Saved file identifier", example = "0a4b3c22-1cb9-41d6-9e2c-7c1b4e2862fa")
    private String fileId;
    @Schema(description = "Display name for the file", example = "lifestyle-preset-pack.zip")
    private String fileName;
    @Schema(description = "CDN URL to stream or download the file", example = "https://video-platform.fra1.cdn.digitaloceanspaces.com/users-content/...")
    private String url;
}
