package com.myproject.video.video_platform.dto.s3_files;

import lombok.Data;

@Data
public class FileS3UploadResponseDto {
    private String fileId;
    private String fileName;
    private String url;
}
