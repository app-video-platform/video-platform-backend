package com.myproject.video.video_platform.entity.products.membership;

import com.myproject.video.video_platform.common.enums.membership.MembershipOrderingMode;
import com.myproject.video.video_platform.entity.products.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "membership_products")
@Getter
@Setter
@NoArgsConstructor
public class MembershipProduct extends Product {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipOrderingMode orderingMode = MembershipOrderingMode.NEWEST_FIRST;
}
