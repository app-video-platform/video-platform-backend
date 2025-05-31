package com.myproject.video.video_platform.entity.products.download;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "files_download_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadProduct {
    @Id
    @GeneratedValue
    private UUID id;

    private String fileName;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private SectionDownloadProduct section;

    private long size;

    private String path;

    private String fileType;

    private String hash;

    private LocalDateTime uploadedAt;

    private int downloadCount;

    private boolean isPublic;
}
