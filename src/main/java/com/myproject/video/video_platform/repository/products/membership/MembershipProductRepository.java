package com.myproject.video.video_platform.repository.products.membership;

import com.myproject.video.video_platform.entity.products.membership.MembershipProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MembershipProductRepository extends JpaRepository<MembershipProduct, UUID> {
}
