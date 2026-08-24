package com.myproject.video.video_platform.service;

import com.myproject.video.video_platform.common.enums.ProductLandingHeroLayout;
import com.myproject.video.video_platform.common.enums.ProductLandingSection;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.landing.ProductLandingPageDtos;
import com.myproject.video.video_platform.entity.ProductLandingPageConfig;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.ProductLandingPageConfigRepository;
import com.myproject.video.video_platform.repository.StorefrontConfigRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.service.product.ProductAuthorizationService;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresentationServicesTest {
    @Mock CurrentUserService currentUserService;
    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock StorefrontConfigRepository storefrontRepository;
    @Mock ProductLandingPageConfigRepository landingRepository;
    @Mock ProductAuthorizationService authorizationService;

    @Test
    void publicStorefrontContainsOnlyPublishedProductsAndNeverLoginEmail() {
        UUID creatorId = UUID.randomUUID();
        User creator = creator(creatorId);
        creator.setEmail("private@example.test");
        creator.setPublicEmail("public@example.test");
        CourseProduct published = product(creator, ProductStatus.PUBLISHED, BigDecimal.ZERO);
        CourseProduct draft = product(creator, ProductStatus.DRAFT, BigDecimal.TEN);
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(storefrontRepository.findByCreatorUserId(creatorId)).thenReturn(Optional.empty());
        when(productRepository.findAllByUser(creator)).thenReturn(List.of(draft, published));

        var result = new StorefrontService(currentUserService, userRepository, productRepository, storefrontRepository)
                .getPublicStorefront(creatorId);

        assertNull(result.creator().email());
        assertEquals("public@example.test", result.creator().publicEmail());
        assertEquals(1, result.products().size());
        assertEquals("free", result.products().get(0).price());
        assertEquals("#ffbd41", result.theme().accentColor());
    }

    @Test
    void publicLandingDefaultsAreNonPersistingAndDraftProductsAreHidden() {
        UUID productId = UUID.randomUUID();
        CourseProduct product = product(creator(UUID.randomUUID()), ProductStatus.PUBLISHED, BigDecimal.ZERO);
        product.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(landingRepository.findByProductId(productId)).thenReturn(Optional.empty());
        ProductLandingPageService service = new ProductLandingPageService(productRepository, landingRepository, authorizationService);

        var defaults = service.getPublic(productId);
        assertNull(defaults.id());
        assertEquals(List.of(ProductLandingSection.CONTENTS, ProductLandingSection.CREATOR), defaults.visibleSections());

        product.setStatus(ProductStatus.DRAFT);
        assertThrows(ResourceNotFoundException.class, () -> service.getPublic(productId));
    }

    @Test
    void landingUpdatePreservesExplicitlyEmptyVisibleSections() {
        UUID productId = UUID.randomUUID();
        CourseProduct product = product(creator(UUID.randomUUID()), ProductStatus.PUBLISHED, BigDecimal.ZERO);
        product.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(landingRepository.findByProductId(productId)).thenReturn(Optional.empty());
        when(landingRepository.save(any())).thenAnswer(invocation -> {
            ProductLandingPageConfig config = invocation.getArgument(0);
            config.setId(UUID.randomUUID());
            config.setUpdatedAt(Instant.parse("2026-08-24T10:00:00Z"));
            return config;
        });
        ProductLandingPageService service = new ProductLandingPageService(productRepository, landingRepository, authorizationService);

        var saved = service.update(productId, new ProductLandingPageDtos.UpdateRequest("Description",
                ProductLandingHeroLayout.MEDIA_LEFT, List.of(), List.of(ProductLandingSection.ABOUT,
                ProductLandingSection.CONTENTS, ProductLandingSection.CREATOR)));

        assertEquals(List.of(), saved.visibleSections());
        verify(authorizationService).requireOwnerOrAdmin(product);
    }

    private static User creator(UUID id) {
        User user = new User();
        user.setUserId(id); user.setFirstName("Alex"); user.setLastName("Creator");
        user.setRoles(Set.of(new Role(1L, "CREATOR")));
        return user;
    }

    private static CourseProduct product(User creator, ProductStatus status, BigDecimal price) {
        CourseProduct product = new CourseProduct();
        product.setId(UUID.randomUUID()); product.setUser(creator); product.setName("Product");
        product.setType(ProductType.COURSE); product.setStatus(status); product.setPrice(price);
        return product;
    }
}
