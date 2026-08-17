package com.myproject.video.video_platform.entity.products;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.common.enums.products.ProductBillingInterval;
import com.myproject.video.video_platform.common.enums.products.ProductCurrency;
import com.myproject.video.video_platform.common.enums.products.ProductPricingModel;
import com.myproject.video.video_platform.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductPricingModel pricingModel = ProductPricingModel.ONE_TIME;

    @Enumerated(EnumType.STRING)
    private ProductBillingInterval billingInterval;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCurrency currency = ProductCurrency.EUR;

    private int customers;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
