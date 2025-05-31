package com.myproject.video.video_platform.repository.products;

import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findAllByUser(User user);
}
