package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.membership.MembershipOrderingMode;
import com.myproject.video.video_platform.common.enums.products.ProductBillingInterval;
import com.myproject.video.video_platform.common.enums.products.ProductCurrency;
import com.myproject.video.video_platform.common.enums.products.ProductPricingModel;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.membership.MembershipProductRequestDto;
import com.myproject.video.video_platform.dto.products.membership.MembershipProductResponseDto;
import com.myproject.video.video_platform.entity.products.membership.MembershipProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.UnsupportedProductOperationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MembershipProductConverter {

    public MembershipProduct fromCreate(MembershipProductRequestDto dto, User owner) {
        MembershipProduct product = new MembershipProduct();
        product.setType(ProductType.MEMBERSHIP);
        product.setUser(owner);
        product.setCustomers(0);
        product.setOrderingMode(MembershipOrderingMode.NEWEST_FIRST);
        product.setName(requireName(dto.getName()));
        product.setDescription(dto.getDescription());
        product.setStatus(parseStatus(dto.getStatus(), ProductStatus.DRAFT));
        product.setPrice(parsePrice(dto.getPrice(), BigDecimal.ZERO));
        applyRecurringPricing(product, dto, ProductBillingInterval.MONTH);
        return product;
    }

    public void applyUpdate(MembershipProduct product, MembershipProductRequestDto dto) {
        if (dto.getName() != null) {
            product.setName(requireName(dto.getName()));
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            product.setStatus(parseStatus(dto.getStatus(), product.getStatus()));
        }
        if (dto.getPrice() != null) {
            product.setPrice(parsePrice(dto.getPrice(), product.getPrice()));
        }
        applyRecurringPricing(product, dto,
                product.getBillingInterval() == null ? ProductBillingInterval.MONTH : product.getBillingInterval());
    }

    public MembershipProductResponseDto toResponse(MembershipProduct product) {
        MembershipProductResponseDto dto = new MembershipProductResponseDto();
        dto.setId(product.getId());
        dto.setType(ProductType.MEMBERSHIP.name());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setStatus(product.getStatus().name());
        dto.setPrice(product.getPrice().compareTo(BigDecimal.ZERO) == 0 ? "free" : product.getPrice().toPlainString());
        dto.setUserId(product.getUser().getUserId());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt() == null ? product.getCreatedAt() : product.getUpdatedAt());
        dto.setPricingModel(ProductPricingModel.RECURRING.name());
        dto.setBillingInterval(product.getBillingInterval().name());
        dto.setCurrency(ProductCurrency.EUR.name());
        dto.setDetails(null);
        return dto;
    }

    private void applyRecurringPricing(MembershipProduct product,
                                       MembershipProductRequestDto dto,
                                       ProductBillingInterval fallbackInterval) {
        if (dto.getPricingModel() != null
                && !ProductPricingModel.RECURRING.name().equalsIgnoreCase(dto.getPricingModel())) {
            throw new IllegalArgumentException("Membership pricingModel must be RECURRING");
        }
        if (dto.getCurrency() != null && !ProductCurrency.EUR.name().equalsIgnoreCase(dto.getCurrency())) {
            throw new IllegalArgumentException("Membership currency must be EUR");
        }
        product.setPricingModel(ProductPricingModel.RECURRING);
        product.setBillingInterval(ProductPricingSupport.parseInterval(dto.getBillingInterval(), fallbackInterval));
        product.setCurrency(ProductCurrency.EUR);
    }

    private ProductStatus parseStatus(String value, ProductStatus fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        ProductStatus status;
        try {
            status = ProductStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown Product status: " + value);
        }
        if (status == ProductStatus.PUBLISHED) {
            throw new UnsupportedProductOperationException(
                    "Membership publishing is unavailable until subscriptions, media delivery, and member access are implemented"
            );
        }
        return status;
    }

    private BigDecimal parsePrice(String value, BigDecimal fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        if ("free".equalsIgnoreCase(value)) {
            return BigDecimal.ZERO.setScale(2);
        }
        try {
            BigDecimal parsed = new BigDecimal(value);
            if (parsed.signum() < 0 || parsed.scale() > 2) {
                throw new IllegalArgumentException("Membership price must be non-negative with at most two decimal places");
            }
            return parsed.setScale(2, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Membership price must be numeric or free");
        }
    }

    private String requireName(String value) {
        if (value == null || value.isBlank() || value.trim().length() > 255) {
            throw new IllegalArgumentException("Membership name is required and must not exceed 255 characters");
        }
        return value.trim();
    }
}
