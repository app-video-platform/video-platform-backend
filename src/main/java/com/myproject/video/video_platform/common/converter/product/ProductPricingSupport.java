package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductBillingInterval;
import com.myproject.video.video_platform.common.enums.products.ProductCurrency;
import com.myproject.video.video_platform.common.enums.products.ProductPricingModel;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.entity.products.Product;

public final class ProductPricingSupport {
    private ProductPricingSupport() {
    }

    public static void initializeOneTime(Product product) {
        product.setPricingModel(ProductPricingModel.ONE_TIME);
        product.setBillingInterval(null);
        product.setCurrency(ProductCurrency.EUR);
    }

    public static void mapResponse(Product product, AbstractProductResponseDto response) {
        ProductPricingModel pricingModel = product.getPricingModel() == null
                ? ProductPricingModel.ONE_TIME
                : product.getPricingModel();
        ProductCurrency currency = product.getCurrency() == null ? ProductCurrency.EUR : product.getCurrency();
        response.setPricingModel(pricingModel.name());
        response.setBillingInterval(product.getBillingInterval() == null ? null : product.getBillingInterval().name());
        response.setCurrency(currency.name());
    }

    public static ProductBillingInterval parseInterval(String value, ProductBillingInterval fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return ProductBillingInterval.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("billingInterval must be MONTH or YEAR");
        }
    }
}
