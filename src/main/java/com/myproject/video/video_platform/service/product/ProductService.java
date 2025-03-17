package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.common.converter.product.DownloadProductConverter;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ProductService {

    private final UserRepository userRepository;
    private final DownloadProductRepository downloadProductRepository;
    private final DownloadProductConverter downloadProductConverter;

    public ProductService(UserRepository userRepository,
                          DownloadProductRepository downloadProductRepository,
                          DownloadProductConverter downloadProductConverter) {
        this.userRepository = userRepository;
        this.downloadProductRepository = downloadProductRepository;
        this.downloadProductConverter = downloadProductConverter;
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


}
