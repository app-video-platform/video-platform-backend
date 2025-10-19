package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.s3_files.ConfirmUploadRequestDto;
import com.myproject.video.video_platform.dto.s3_files.FileS3UploadResponseDto;
import com.myproject.video.video_platform.dto.s3_files.PresignedUrlResponseDto;
import com.myproject.video.video_platform.service.product.FilesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class FilesController {

    private final FilesService filesService;

    /**
     * Returns a pre-signed URL for uploading a file directly to Spaces.
     *
     * @param sectionId  ID of the section
     * @param folderType one of UploadFolder values
     * @param filename   original file name
     */
    @GetMapping("/presigned-url")
    @Operation(summary = "Request upload URL", description = "Generates a short-lived pre-signed PUT URL for uploading a file directly to Spaces. Ownership of the section is verified via JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL generated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PresignedUrlResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the section",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Section not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "Confirm upload", description = "Persists metadata for a completed Spaces upload and returns the CDN URL.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Upload recorded",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FileS3UploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the section",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Section not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FileS3UploadResponseDto> confirmUpload(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the completed upload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ConfirmUploadRequestDto.class),
                            examples = @ExampleObject(value = "{\n  \"sectionId\": \"c5c4a1d0-8d61-4ff0-9dbe-2a4d5e3da5b1\",\n  \"key\": \"users-content/...\",\n  \"fileName\": \"lifestyle-preset-pack.zip\",\n  \"fileSize\": 48234123\",\n  \"fileType\": \"application/zip\"\n}")))
            @RequestBody ConfirmUploadRequestDto dto) {

        log.info("Confirming upload for section={}, key={}", dto.getSectionId(), dto.getKey());
        FileS3UploadResponseDto response = filesService.confirmUpload(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
