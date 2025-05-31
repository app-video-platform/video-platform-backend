package com.myproject.video.video_platform.repository.products.download;

import com.myproject.video.video_platform.entity.products.download.FileDownloadProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileDownloadProductRepository extends JpaRepository<FileDownloadProduct, UUID> {
}
