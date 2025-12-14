package com.myproject.video.video_platform.controller.docs.product.download;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.s3_files.ConfirmUploadRequestDto;
import com.myproject.video.video_platform.dto.s3_files.FileS3UploadResponseDto;
import com.myproject.video.video_platform.dto.s3_files.PresignedUrlResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public interface DownloadFilesApiDoc {

    @Operation(
            summary = "Request upload URL",
            description = "Generates a short-lived pre-signed PUT URL for uploading a file directly to Spaces. Ownership of the section is verified via JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL generated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PresignedUrlResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the section",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Section not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<PresignedUrlResponseDto> getPresignedUrl(String sectionId, String folderType, String filename);

    @Operation(
            summary = "Confirm upload",
            description = "Persists metadata for a completed Spaces upload and returns the CDN URL.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the completed upload",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConfirmUploadRequestDto.class),
                            examples = @ExampleObject(
                                    value = "{\n  \"sectionId\": \"c5c4a1d0-8d61-4ff0-9dbe-2a4d5e3da5b1\",\n  \"key\": \"users-content/...\",\n  \"fileName\": \"lifestyle-preset-pack.zip\",\n  \"fileSize\": 48234123\",\n  \"fileType\": \"application/zip\"\n}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Upload recorded",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FileS3UploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the section",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Section not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<FileS3UploadResponseDto> confirmUpload(ConfirmUploadRequestDto dto);
}

