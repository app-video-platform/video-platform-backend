package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.entity.StorefrontConfig;
import com.myproject.video.video_platform.repository.ProductLandingPageConfigRepository;
import com.myproject.video.video_platform.repository.StorefrontConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductPresentationCleanupService {
    private final ProductLandingPageConfigRepository landingPageRepository;
    private final StorefrontConfigRepository storefrontRepository;

    public void removeProductReferences(UUID productId) {
        landingPageRepository.deleteByProductId(productId);
        for (StorefrontConfig config : storefrontRepository.findAll()) {
            boolean changed = config.getProductOrderIds().removeIf(productId::equals);
            if (productId.equals(config.getFeaturedProductId())) {
                config.setFeaturedProductId(null);
                changed = true;
            }
            if (changed) storefrontRepository.save(config);
        }
    }
}
