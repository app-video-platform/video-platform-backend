package com.myproject.video.video_platform.service;

import com.myproject.video.video_platform.common.enums.ProductLandingHeroLayout;
import com.myproject.video.video_platform.common.enums.ProductLandingSection;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.dto.products.landing.ProductLandingPageDtos;
import com.myproject.video.video_platform.entity.ProductLandingPageConfig;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.ProductLandingPageConfigRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.service.product.ProductAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductLandingPageService {
    private static final List<ProductLandingSection> DEFAULT_VISIBLE = List.of(
            ProductLandingSection.CONTENTS, ProductLandingSection.CREATOR);
    private static final List<ProductLandingSection> DEFAULT_ORDER = List.of(
            ProductLandingSection.ABOUT, ProductLandingSection.CONTENTS, ProductLandingSection.CREATOR);

    private final ProductRepository productRepository;
    private final ProductLandingPageConfigRepository configRepository;
    private final ProductAuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public ProductLandingPageDtos.Config getForManager(UUID productId) {
        Product product = product(productId);
        authorizationService.requireOwnerOrAdmin(product);
        return configRepository.findByProductId(productId).map(ProductLandingPageService::dto)
                .orElseGet(() -> defaults(productId));
    }

    @Transactional(readOnly = true)
    public ProductLandingPageDtos.Config getPublic(UUID productId) {
        Product product = product(productId);
        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Product landing page not found");
        }
        return configRepository.findByProductId(productId).map(ProductLandingPageService::dto)
                .orElseGet(() -> defaults(productId));
    }

    @Transactional
    public ProductLandingPageDtos.Config update(UUID productId, ProductLandingPageDtos.UpdateRequest request) {
        Product product = product(productId);
        authorizationService.requireOwnerOrAdmin(product);
        validateSections(request.visibleSections(), request.sectionOrder());
        ProductLandingPageConfig config = configRepository.findByProductId(productId).orElseGet(() -> {
            ProductLandingPageConfig created = new ProductLandingPageConfig();
            created.setProductId(productId);
            return created;
        });
        config.setMarketingDescription(request.marketingDescription());
        config.setHeroLayout(request.heroLayout());
        config.getVisibleSections().clear();
        config.getVisibleSections().addAll(request.visibleSections());
        config.getSectionOrder().clear();
        config.getSectionOrder().addAll(request.sectionOrder());
        return dto(configRepository.save(config));
    }

    private Product product(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private static void validateSections(List<ProductLandingSection> visible, List<ProductLandingSection> order) {
        if (new HashSet<>(visible).size() != visible.size()) {
            throw new IllegalArgumentException("visibleSections must not contain duplicates");
        }
        Set<ProductLandingSection> expected = Set.of(ProductLandingSection.values());
        if (order.size() != expected.size() || !new HashSet<>(order).equals(expected)) {
            throw new IllegalArgumentException("sectionOrder must contain ABOUT, CONTENTS, and CREATOR exactly once");
        }
    }

    private static ProductLandingPageDtos.Config defaults(UUID productId) {
        return new ProductLandingPageDtos.Config(null, productId.toString(), "",
                ProductLandingHeroLayout.MEDIA_RIGHT, DEFAULT_VISIBLE, DEFAULT_ORDER, null);
    }

    private static ProductLandingPageDtos.Config dto(ProductLandingPageConfig config) {
        return new ProductLandingPageDtos.Config(config.getId().toString(), config.getProductId().toString(),
                config.getMarketingDescription(), config.getHeroLayout(), List.copyOf(config.getVisibleSections()),
                List.copyOf(config.getSectionOrder()), config.getUpdatedAt());
    }
}
