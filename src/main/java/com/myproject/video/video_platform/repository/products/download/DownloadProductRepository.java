package com.myproject.video.video_platform.repository.products.download;

import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DownloadProductRepository extends JpaRepository<DownloadProduct, UUID> {
    List<DownloadProduct> findAllByUser(User user);

    /** Eagerly fetch sections and their files in one query */
    @Query("""
        select distinct dp from DownloadProduct dp
        left join fetch dp.sectionDownloadProducts sec
        left join fetch sec.files
        where dp.id = :id
    """)
    Optional<DownloadProduct> findFullById(@Param("id") UUID id);
}
