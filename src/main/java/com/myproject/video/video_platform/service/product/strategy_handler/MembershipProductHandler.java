package com.myproject.video.video_platform.service.product.strategy_handler;

import com.myproject.video.video_platform.common.converter.product.MembershipProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.membership.MembershipProductRequestDto;
import com.myproject.video.video_platform.entity.products.membership.MembershipProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.membership.MembershipProductRepository;
import com.myproject.video.video_platform.repository.products.membership.MembershipContentRepository;
import com.myproject.video.video_platform.repository.products.membership.MembershipFeedEntryRepository;
import com.myproject.video.video_platform.service.product.ProductAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MembershipProductHandler implements ProductTypeHandler {
    private final MembershipProductRepository repository;
    private final MembershipProductConverter converter;
    private final ProductAuthorizationService authorizationService;
    private final MembershipContentRepository contentRepository;
    private final MembershipFeedEntryRepository feedRepository;

    @Override
    public ProductType getSupportedType() {
        return ProductType.MEMBERSHIP;
    }

    @Override
    @Transactional
    public AbstractProductResponseDto createProduct(AbstractProductRequestDto baseDto) {
        MembershipProductRequestDto dto = (MembershipProductRequestDto) baseDto;
        User owner = authorizationService.resolveOwnerForCreate(dto);
        return converter.toResponse(repository.saveAndFlush(converter.fromCreate(dto, owner)));
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractProductResponseDto getProductById(String productId) {
        MembershipProduct product = find(productId);
        return converter.toResponse(product);
    }

    @Override
    @Transactional
    public AbstractProductResponseDto updateProduct(AbstractProductRequestDto baseDto) {
        MembershipProductRequestDto dto = (MembershipProductRequestDto) baseDto;
        MembershipProduct product = find(dto.getId());
        authorizationService.requireOwnerOrAdmin(product);
        converter.applyUpdate(product, dto);
        return converter.toResponse(repository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(String userId, String productId) {
        MembershipProduct product = find(productId);
        authorizationService.requireOwnerOrAdmin(product);
        feedRepository.deleteAllByMembershipId(product.getId());
        contentRepository.deleteAllByMembershipId(product.getId());
        repository.delete(product);
    }

    private MembershipProduct find(String productId) {
        return repository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found: " + productId));
    }
}
