package com.myproject.video.video_platform.repository.products;

import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findAllByUser(User user);

    /**
     * EXPLORE: search across all products by product.name OR user.firstName OR user.lastName
     */
    @Query("""
    SELECT p
      FROM Product p
      JOIN p.user u
     WHERE lower(p.name)       LIKE %:term%
        OR lower(u.firstName)  LIKE %:term%
        OR lower(u.lastName)   LIKE %:term%
    """)
    Page<Product> searchByNameOrOwner(
            @Param("term") String term,
            Pageable pageable
    );

    /**
     * LIBRARY / TEACHER page: search this user’s products by product.name only
     */
    @Query("""
    SELECT p
      FROM Product p
     WHERE p.user.id = :userId
       AND lower(p.name) LIKE %:term%
    """)
    Page<Product> searchByUserAndName(
            @Param("userId") UUID userId,
            @Param("term")   String term,
            Pageable pageable
    );
}
