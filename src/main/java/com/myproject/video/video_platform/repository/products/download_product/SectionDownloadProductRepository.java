package com.myproject.video.video_platform.repository.products.download_product;

import com.myproject.video.video_platform.entity.products.download.SectionDownloadProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SectionDownloadProductRepository extends JpaRepository<SectionDownloadProduct, UUID> {
}
