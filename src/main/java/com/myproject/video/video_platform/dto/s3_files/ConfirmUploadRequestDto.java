package com.myproject.video.video_platform.dto.s3_files;

import lombok.Data;

@Data
public class ConfirmUploadRequestDto {
    private String sectionId;
    private String key;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileType;
}
