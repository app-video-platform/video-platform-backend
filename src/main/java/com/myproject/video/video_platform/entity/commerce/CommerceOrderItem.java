package com.myproject.video.video_platform.entity.commerce;

import com.myproject.video.video_platform.common.enums.products.ProductType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "commerce_order_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_commerce_order_items_order_product",
                columnNames = {"order_id", "product_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class CommerceOrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CommerceOrder order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 40)
    private ProductType productType;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "unit_amount_minor", nullable = false)
    private long unitAmountMinor;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_total_minor", nullable = false)
    private long lineTotalMinor;
}
