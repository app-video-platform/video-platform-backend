package com.myproject.video.video_platform.controller.product.download;

import com.myproject.video.video_platform.controller.docs.product.download.DownloadFilesApiDoc;
import com.myproject.video.video_platform.dto.s3_files.ConfirmUploadRequestDto;
import com.myproject.video.video_platform.dto.s3_files.FileS3UploadResponseDto;
import com.myproject.video.video_platform.dto.s3_files.PresignedUrlResponseDto;
import com.myproject.video.video_platform.service.product.FilesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/files")
@Setter
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Files", description = "DigitalOcean Spaces upload helpers for course and download assets.")
public class DownloadFilesController implements DownloadFilesApiDoc {

    private final FilesService filesService;

    /**
     * Returns a pre-signed URL for uploading a file directly to Spaces.
     *
     * @param sectionId  ID of the section
     * @param folderType one of UploadFolder values
     * @param filename   original file name
     */
    @GetMapping("/presigned-url")
    @Override
    public ResponseEntity<PresignedUrlResponseDto> getPresignedUrl(
            @RequestParam String sectionId,
            @RequestParam String folderType,
            @RequestParam String filename
    ) {
        PresignedUrlResponseDto dto = filesService.generatePresignedUrl(
                sectionId, folderType, filename, Duration.ofMinutes(5)
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/confirm-upload")
    @Override
    public ResponseEntity<FileS3UploadResponseDto> confirmUpload(
            @RequestBody ConfirmUploadRequestDto dto) {

        log.info("Confirming upload for section={}, key={}", dto.getSectionId(), dto.getKey());
        FileS3UploadResponseDto response = filesService.confirmUpload(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
