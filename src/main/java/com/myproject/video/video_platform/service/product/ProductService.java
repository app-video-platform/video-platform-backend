package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.common.converter.product.DownloadProductConverter;
import com.myproject.video.video_platform.common.converter.product.ProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.ProductMinimised;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.InvalidProductTypeException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.repository.products.download.DownloadProductRepository;
import com.myproject.video.video_platform.service.product.strategy_handler.ProductTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    private final UserRepository userRepository;
    private final DownloadProductRepository downloadProductRepository;
    private final DownloadProductConverter downloadProductConverter;
    private final ProductRepository productRepository;
    private final Map<ProductType, ProductTypeHandler> handlers;
    private final ProductConverter productConverter;

    public ProductService(UserRepository userRepository,
                          DownloadProductRepository downloadProductRepository,
                          DownloadProductConverter downloadProductConverter,
                          ProductRepository productRepository,
                          Set<ProductTypeHandler> handlerSet,
                          ProductConverter productConverter) {
        this.userRepository = userRepository;
        this.downloadProductRepository = downloadProductRepository;
        this.productRepository = productRepository;
        this.downloadProductConverter = downloadProductConverter;
        this.productConverter = productConverter;

        // Convert the set of handlers into a map: ProductType -> handler
        this.handlers = handlerSet.stream()
                .collect(Collectors.toMap(
                        ProductTypeHandler::getSupportedType,
                        Function.identity()
                ));
    }


    public AbstractProductResponseDto createProduct(AbstractProductRequestDto dto) {
        return getProductStrategyHandler(dto.getType()).createProduct(dto);
    }


    public AbstractProductResponseDto getProductByIdAndType(String productId, String type) {
        return getProductStrategyHandler(type).getProductById(productId);
    }

    public AbstractProductResponseDto updateProduct(AbstractProductRequestDto dto) {
        return getProductStrategyHandler(dto.getType()).updateProduct(dto);
    }

    public List<AbstractProductResponseDto> getAllProductsForUser(String userId) {
        User user = userRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        List<Product> products = productRepository.findAllByUser(user);

        return products.stream()
                .map(product -> getProductStrategyHandler(product.getType().name())
                        .getProductById(product.getId().toString()))
                .toList();
    }


    private ProductTypeHandler getProductStrategyHandler(String typeStr) {
        ProductType type;

        try {
            type = ProductType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidProductTypeException("Unknown product type: " + typeStr);
        }

        ProductTypeHandler handler = handlers.get(type);
        if (handler == null) {
            throw new InvalidProductTypeException("No handler for product type: " + type);
        }
        return handler;
    }


    public void deleteProduct(String userId, String productId, String productType) {
        getProductStrategyHandler(productType).deleteProduct(userId, productId);
    }

    public List<ProductMinimised> getAllProductsMinimised() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(productConverter::mapProductMinimisedToResponse)
                .toList();
    }

    public List<ProductMinimised> getAllProductsMinimisedForUser(String userId) {
        User user = userRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        List<Product> products = productRepository.findAllByUser(user);

        return products.stream()
                .map(productConverter::mapProductMinimisedToResponse)
                .toList();
    }

    /** EXPLORE: all products by name or owner */
    public Page<ProductMinimised> searchAllProducts(String term, Pageable pageable) {
        String normalized = term.trim().toLowerCase();
        return productRepository.searchByNameOrOwner(normalized, pageable)
                .map(productConverter::mapProductMinimisedToResponse);
    }

    /** LIBRARY/TEACHER: this user’s products by name */
    public Page<ProductMinimised> searchUserProducts(String userIdStr, String term, Pageable pageable) {
        UUID userId = UUID.fromString(userIdStr);

        userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userIdStr));

        String normalized = term.trim().toLowerCase();
        return productRepository.searchByUserAndName(userId, normalized, pageable)
                .map(productConverter::mapProductMinimisedToResponse);
    }
}
