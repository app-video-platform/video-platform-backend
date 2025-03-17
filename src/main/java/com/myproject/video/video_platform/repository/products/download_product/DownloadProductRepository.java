package com.myproject.video.video_platform.repository.products.download_product;

import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.download_product.DownloadProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DownloadProductRepository extends JpaRepository<DownloadProduct, UUID> {
    List<DownloadProduct> findAllByUser(User user);
}
