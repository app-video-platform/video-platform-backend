package com.myproject.video.video_platform.dto.s3_files;

import lombok.Data;

@Data
public class PresignedUrlResponseDto {
    private String fileId;
    private String presignedUrl;
    private String key;
    private String fileUrl;
}
