package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.common.enums.s3folders.S3UploadFolder;
import com.myproject.video.video_platform.dto.s3_files.ConfirmUploadRequestDto;
import com.myproject.video.video_platform.dto.s3_files.FileS3UploadResponseDto;
import com.myproject.video.video_platform.dto.s3_files.PresignedUrlResponseDto;
import com.myproject.video.video_platform.entity.products.download.FileDownloadProduct;
import com.myproject.video.video_platform.entity.products.download.SectionDownloadProduct;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.download_product.FileDownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download_product.SectionDownloadProductRepository;
import com.myproject.video.video_platform.service.digitalocean.SpacesS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilesService {

    @Value("${digitalocean.spaces.cdnEndpointUrl}")
    private String cdnEndpointUrl;

    @Value("${digitalocean.spaces.bucket-media}")
    private String s3mediaBucket;

    private final SpacesS3Service spacesS3Service;
    private final SectionDownloadProductRepository sectionRepository;
    private final FileDownloadProductRepository fileRepository;


    /**
     * Build key, check ownership, sign URL, return all info.
     */
    public PresignedUrlResponseDto generatePresignedUrl(
            String sectionIdStr,
            String folderTypeStr,
            String filename,
            Duration expiration
    ) {
        SectionDownloadProduct section = authorizeSectionUpload(sectionIdStr);
        S3UploadFolder folder  = S3UploadFolder.fromString(folderTypeStr);
        UUID fileId = UUID.randomUUID();

        String key = buildKey(section, folder, filename, fileId);
        String presigned = spacesS3Service.generatePresignedUrlForPut(key, expiration);
        String fileUrl   = cdnEndpointUrl + "/" + s3mediaBucket + "/" + key;

        PresignedUrlResponseDto dto = new PresignedUrlResponseDto();
        dto.setFileId(fileId.toString());
        dto.setPresignedUrl(presigned);
        dto.setKey(key);
        dto.setFileUrl(fileUrl);
        return dto;
    }

    /** throws if the current user isn’t owner of that section */
    private SectionDownloadProduct authorizeSectionUpload(String sectionIdStr) {
        UUID sectionId = UUID.fromString(sectionIdStr);
        String email   = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        SectionDownloadProduct section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        String ownerEmail = section.getDownloadProduct().getUser().getEmail();
        if (!ownerEmail.equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Not authorized for section " + sectionId);
        }
        return section;
    }

    /** place to construct S3 path for ANY upload type */
    private String buildKey(SectionDownloadProduct section,
                            S3UploadFolder folder,
                            String filename, UUID fileId) {

        String userId     = section.getDownloadProduct().getUser().getUserId().toString();
        String sectionId  = section.getId().toString();
        String safeName   = filename.replaceAll("\\s+", "_");
        return String.join("/",
                "users-content",
                userId,
                folder.getFolderName(),
                sectionId,
                fileId + "_" + safeName
        );
    }

    public FileS3UploadResponseDto confirmUpload(ConfirmUploadRequestDto dto) {
        SectionDownloadProduct section = authorizeSectionUpload(dto.getSectionId());

        FileDownloadProduct fileEntity = new FileDownloadProduct();
        fileEntity.setFileName(dto.getFileName());
        fileEntity.setUserId(section.getDownloadProduct().getUser().getUserId());
        fileEntity.setSection(section);
        fileEntity.setSize(dto.getFileSize());
        fileEntity.setPath(dto.getKey());
        fileEntity.setFileType(dto.getFileType());
        fileEntity.setUploadedAt(LocalDateTime.now());
        fileEntity.setDownloadCount(0);
        fileEntity = fileRepository.save(fileEntity);

        FileS3UploadResponseDto resp = new FileS3UploadResponseDto();
        resp.setFileId  (fileEntity.getId().toString());
        resp.setFileName(fileEntity.getFileName());

        resp.setUrl(dto.getFileUrl() != null
                ? dto.getFileUrl()
                : cdnEndpointUrl + "/" + s3mediaBucket + "/" + dto.getKey());
        return resp;
    }
}
