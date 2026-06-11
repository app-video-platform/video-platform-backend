package com.myproject.video.video_platform.service.admin;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.common.converter.product.ProductConverter;
import com.myproject.video.video_platform.dto.products.ProductMinimised;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductConverter productConverter;

    public AdminProductService(ProductRepository productRepository,
                               ProductConverter productConverter) {
        this.productRepository = productRepository;
        this.productConverter = productConverter;
    }

    @Transactional(readOnly = true)
    public Page<ProductMinimised> searchProducts(String search,
                                                 String ownerId,
                                                 ProductType type,
                                                 ProductStatus status,
                                                 Pageable pageable) {
        return productRepository.findAll(productSpec(search, ownerId, type, status), pageable)
                .map(productConverter::mapProductMinimisedToResponse);
    }

    private Specification<Product> productSpec(String search,
                                               String ownerId,
                                               ProductType type,
                                               ProductStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("description")), term),
                        cb.like(cb.lower(root.get("user").get("firstName")), term),
                        cb.like(cb.lower(root.get("user").get("lastName")), term)
                ));
            }
            if (ownerId != null && !ownerId.isBlank()) {
                predicates.add(cb.equal(root.get("user").get("userId"), UUID.fromString(ownerId)));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
