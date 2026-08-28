package com.myproject.video.video_platform.entity.products;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_media")
@Getter
@Setter
public class ProductMedia {
    @Id @GeneratedValue private UUID id;
    @Column(name = "product_id", nullable = false) private UUID productId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private Kind kind;
    @Column(name = "object_key", nullable = false, unique = true, length = 512) private String objectKey;
    @Column(name = "cdn_url", nullable = false, length = 2048) private String cdnUrl;
    @Column(name = "file_name", nullable = false) private String fileName;
    @Column(name = "mime_type", nullable = false, length = 100) private String mimeType;
    @Column(name = "file_size", nullable = false) private long size;
    @Column(name = "gallery_position") private Integer galleryPosition;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private Status status;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;

    public enum Kind { THUMBNAIL, GALLERY_IMAGE, PROMO_VIDEO }
    public enum Status { READY }
}
