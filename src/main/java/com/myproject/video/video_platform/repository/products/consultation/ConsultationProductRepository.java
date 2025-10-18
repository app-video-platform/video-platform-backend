package com.myproject.video.video_platform.repository.products.consultation;

import com.myproject.video.video_platform.entity.products.consultation.ConsultationProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsultationProductRepository extends JpaRepository<ConsultationProduct, UUID> {
}
