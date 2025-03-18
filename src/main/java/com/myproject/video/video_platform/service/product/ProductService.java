package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.common.converter.product.DownloadProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products_creation.ConsultationProductRequestDto;
import com.myproject.video.video_platform.dto.products_creation.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products_creation.DownloadProductRequestDto;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.download_product.DownloadProduct;
import com.myproject.video.video_platform.exception.product.InvalidProductTypeException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.download_product.DownloadProductRepository;
import com.myproject.video.video_platform.service.product.strategy_handler.ProductTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final Map<ProductType, ProductTypeHandler> handlers;

    public ProductService(UserRepository userRepository,
                          DownloadProductRepository downloadProductRepository,
                          DownloadProductConverter downloadProductConverter,
                          Set<ProductTypeHandler> handlerSet) {
        this.userRepository = userRepository;
        this.downloadProductRepository = downloadProductRepository;
        this.downloadProductConverter = downloadProductConverter;

        // Convert the set of handlers into a map: ProductType -> handler
        this.handlers = handlerSet.stream()
                .collect(Collectors.toMap(
                        ProductTypeHandler::getSupportedType,
                        Function.identity()
                ));
    }


    public AbstractProductResponseDto createProduct(AbstractProductRequestDto dto) {
        // Polymorphic check
        if (dto instanceof DownloadProductRequestDto) {
            return createDownloadProduct((DownloadProductRequestDto) dto);
        } else if (dto instanceof CourseProductRequestDto) {
            //return createCourseProduct((CourseProductRequestDto) dto);
            throw new InvalidProductTypeException("Unknown product type: " + dto.getType());
        } else if (dto instanceof ConsultationProductRequestDto) {
            //return createConsultationProduct((ConsultationProductRequestDto) dto);
            throw new InvalidProductTypeException("Unknown product type: " + dto.getType());
        } else {
            throw new InvalidProductTypeException("Unknown product type: " + dto.getType());
        }
    }

    private AbstractProductResponseDto createDownloadProduct(DownloadProductRequestDto dto) {
        log.info("Creating a DownloadProduct: {}", dto.getName());

        Optional<User> userOptional = userRepository.findByUserId(UUID.fromString(dto.getUserId()));
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found with id: " + dto.getUserId());
        }

        DownloadProduct product = downloadProductConverter.mapDownloadProductRequestDtoToEntity(dto, userOptional.get());
        DownloadProduct saved = downloadProductRepository.save(product);

        log.info("Created succesfully a DownloadProduct: {}", dto.getName());
        return downloadProductConverter.mapDownloadProductToResponse(saved);
    }


    public List<AbstractProductResponseDto> getAllDownloadProductsForUser(String userId) {
        Optional<User> userOptional = userRepository.findByUserId(UUID.fromString(userId));
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        return downloadProductRepository.findAllByUser(userOptional.get())
                .stream()
                .map(downloadProductConverter::mapDownloadProductToResponse)
                .toList();
    }

    public AbstractProductResponseDto getProductByIdAndType(String productId, String typeStr) {
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

        return handler.getProductById(productId);
    }
}
