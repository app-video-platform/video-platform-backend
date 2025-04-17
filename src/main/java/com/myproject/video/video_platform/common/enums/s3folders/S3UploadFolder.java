package com.myproject.video.video_platform.common.enums.s3folders;

public enum S3UploadFolder {

    DOWNLOAD_SECTION_FILES("download_section_files"),
    COURSE_SECTION_FILES("course_section-files"),
    CONSULTATION_FILES("consultation_files");

    private final String folderName;

    S3UploadFolder(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    public static S3UploadFolder fromString(String name) {
        return S3UploadFolder.valueOf(name.toUpperCase());
    }
}
