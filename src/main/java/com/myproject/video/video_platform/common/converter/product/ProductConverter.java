package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductResponseDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductResponseDto;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import org.springframework.stereotype.Component;

@Component
public class ProductConverter {


    public AbstractProductResponseDto mapProductToResponse(Product product) {
        AbstractProductResponseDto dto;

        if (product instanceof DownloadProduct) {
            dto = new DownloadProductResponseDto();
        } else if (product instanceof CourseProduct) {
            dto = new CourseProductResponseDto();
//        } else if (product instanceof ConsultationProduct) {
//            dto = new ConsultationProductResponseDto();
        } else {
            throw new IllegalArgumentException("Unknown product subclass: " + product.getClass().getSimpleName());
        }

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setStatus(product.getStatus().name());
        dto.setPrice(product.getPrice().toPlainString());
        dto.setUserId(product.getUser().getUserId());
        dto.setType(product.getType().name());

        return dto;
    }
}
