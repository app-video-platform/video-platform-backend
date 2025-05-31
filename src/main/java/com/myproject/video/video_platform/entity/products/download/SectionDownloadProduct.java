package com.myproject.video.video_platform.entity.products.download;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "sections_download_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionDownloadProduct {
    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    @Column(length = 1000)
    private String description;

    private Integer position;

    @ManyToOne
    @JoinColumn(name = "download_product_id", nullable = false)
    private DownloadProduct downloadProduct;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("uploadedAt ASC")
    private Set<FileDownloadProduct> files = new LinkedHashSet<>();
}
