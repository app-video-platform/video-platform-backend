package com.myproject.video.video_platform.entity.products.download_product;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sections_download_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionDownloadProduct {
    @Id
    private UUID id;

    private String title;

    @Column(length = 1000)
    private String description;

    private Integer position;

    @ManyToOne
    @JoinColumn(name = "download_product_id", nullable = false)
    private DownloadProduct downloadProduct;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileDownloadProduct> files;
}
