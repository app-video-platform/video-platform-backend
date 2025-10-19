package com.myproject.video.video_platform.dto.s3_files;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Details required to perform a direct Spaces upload.")
public class PresignedUrlResponseDto {
    @Schema(description = "Temporary identifier correlating the upload", example = "0a4b3c22-1cb9-41d6-9e2c-7c1b4e2862fa")
    private String fileId;
    @Schema(description = "Pre-signed PUT URL", example = "https://fra1.digitaloceanspaces.com/video-platform/users-content/...&X-Amz-Signature=...")
    private String presignedUrl;
    @Schema(description = "Spaces object key that must be used when confirming", example = "users-content/.../0a4b3c22-1cb9-41d6-9e2c-7c1b4e2862fa_lifestyle-preset-pack.zip")
    private String key;
    @Schema(description = "Public CDN URL computed for the uploaded file", example = "https://video-platform.fra1.cdn.digitaloceanspaces.com/users-content/...")
    private String fileUrl;
}
