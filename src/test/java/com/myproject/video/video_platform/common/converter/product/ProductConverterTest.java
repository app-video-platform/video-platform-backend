package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductResponseDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductResponseDto;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductConverterTest {

    private ProductConverter converter;

    @BeforeEach
    void setup() {
        converter = new ProductConverter();
    }

    @Test
    void mapProductToResponse_downloadProduct() {
        User user = new User();
        user.setUserId(UUID.randomUUID());

        DownloadProduct product = new DownloadProduct();
        product.setId(UUID.randomUUID());
        product.setName("Pack");
        product.setDescription("desc");
        product.setStatus(ProductStatus.PUBLISHED);
        product.setPrice(new BigDecimal("9.99"));
        product.setUser(user);
        product.setType(ProductType.DOWNLOAD);

        AbstractProductResponseDto dto = converter.mapProductToResponse(product);

        assertTrue(dto instanceof DownloadProductResponseDto);
        assertEquals(product.getId(), dto.getId());
        assertEquals("Pack", dto.getName());
        assertEquals("PUBLISHED", dto.getStatus());
        assertEquals("9.99", dto.getPrice());
        assertEquals(user.getUserId(), dto.getUserId());
        assertEquals("DOWNLOAD", dto.getType());
    }

    @Test
    void mapProductToResponse_courseProduct() {
        User user = new User();
        user.setUserId(UUID.randomUUID());

        CourseProduct product = new CourseProduct();
        product.setId(UUID.randomUUID());
        product.setName("Course");
        product.setDescription("desc");
        product.setStatus(ProductStatus.DRAFT);
        product.setPrice(BigDecimal.ZERO);
        product.setUser(user);
        product.setType(ProductType.COURSE);

        AbstractProductResponseDto dto = converter.mapProductToResponse(product);

        assertTrue(dto instanceof CourseProductResponseDto);
        assertEquals(product.getId(), dto.getId());
        assertEquals("Course", dto.getName());
        assertEquals("DRAFT", dto.getStatus());
        assertEquals("0", dto.getPrice());
        assertEquals(user.getUserId(), dto.getUserId());
        assertEquals("COURSE", dto.getType());
    }

    @Test
    void mapProductToResponse_unknownSubclass_throws() {
        class UnknownProduct extends DownloadProduct {}
        UnknownProduct unknown = new UnknownProduct();
        unknown.setUser(new User());
        assertThrows(IllegalArgumentException.class, () -> converter.mapProductToResponse(unknown));
    }
}
