package com.myproject.video.video_platform.controller.product.download;

import com.myproject.video.video_platform.dto.s3_files.ConfirmUploadRequestDto;
import com.myproject.video.video_platform.dto.s3_files.FileS3UploadResponseDto;
import com.myproject.video.video_platform.dto.s3_files.PresignedUrlResponseDto;
import com.myproject.video.video_platform.service.product.FilesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/sections/{sectionId}/files")
@Tag(name = "Download Files", description = "Canonical nested endpoints for downloadable section files.")
public class ProductDownloadFilesController {

    private final FilesService filesService;

    @GetMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponseDto> getPresignedUrl(
            @PathVariable("productId") String productId,
            @PathVariable("sectionId") String sectionId,
            @RequestParam("filename") String filename
    ) {
        PresignedUrlResponseDto response = filesService.generatePresignedUrl(
                productId,
                sectionId,
                filename,
                Duration.ofMinutes(5)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-upload")
    public ResponseEntity<FileS3UploadResponseDto> confirmUpload(
            @PathVariable("productId") String productId,
            @PathVariable("sectionId") String sectionId,
            @RequestBody ConfirmUploadRequestDto request
    ) {
        FileS3UploadResponseDto response = filesService.confirmUpload(productId, sectionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable("productId") String productId,
            @PathVariable("sectionId") String sectionId,
            @PathVariable("fileId") String fileId
    ) {
        filesService.deleteFile(productId, sectionId, fileId);
        return ResponseEntity.noContent().build();
    }
}
