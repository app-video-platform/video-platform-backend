package com.myproject.video.video_platform.entity.products;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Product {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Column(length = 420)
    private String description;

    private String image;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private BigDecimal price;

    private int customers;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
