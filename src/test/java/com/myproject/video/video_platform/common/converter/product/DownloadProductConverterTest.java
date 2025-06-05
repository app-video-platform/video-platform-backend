package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductResponseDto;
import com.myproject.video.video_platform.dto.products.download.SectionDownloadProductRequestDto;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import com.myproject.video.video_platform.entity.products.download.FileDownloadProduct;
import com.myproject.video.video_platform.entity.products.download.SectionDownloadProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DownloadProductConverterTest {

    private DownloadProductConverter converter;

    @BeforeEach
    void setup() {
        converter = new DownloadProductConverter();
        setField("cdnEndpoint", "http://cdn");
        setField("bucketName", "bucket");
    }

    private void setField(String name, String value) {
        try {
            Field f = DownloadProductConverter.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(converter, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void mapDownloadProductRequestDtoToEntity_setsFields() {
        DownloadProductRequestDto dto = new DownloadProductRequestDto();
        dto.setName("Prod");
        dto.setDescription("desc");
        dto.setStatus("published");
        dto.setPrice("5");
        dto.setType("DOWNLOAD");
        SectionDownloadProductRequestDto sec = new SectionDownloadProductRequestDto();
        sec.setTitle("sec");
        sec.setDescription("d");
        sec.setPosition(1);
        dto.setSections(List.of(sec));

        User user = new User();
        user.setUserId(UUID.randomUUID());

        DownloadProduct product = converter.mapDownloadProductRequestDtoToEntity(dto, user);

        assertEquals("Prod", product.getName());
        assertEquals(ProductStatus.PUBLISHED, product.getStatus());
        assertEquals(new BigDecimal("5"), product.getPrice());
        assertEquals(user, product.getUser());
        assertEquals(1, product.getSectionDownloadProducts().size());
    }

    @Test
    void mapDownloadProductToResponse_buildsFileUrls() {
        User user = new User();
        user.setUserId(UUID.randomUUID());

        DownloadProduct product = new DownloadProduct();
        product.setId(UUID.randomUUID());
        product.setName("Prod");
        product.setDescription("desc");
        product.setStatus(ProductStatus.PUBLISHED);
        product.setPrice(BigDecimal.ZERO);
        product.setUser(user);
        product.setType(ProductType.DOWNLOAD);

        SectionDownloadProduct section = new SectionDownloadProduct();
        section.setId(UUID.randomUUID());
        section.setTitle("sec");
        section.setPosition(1);
        section.setDownloadProduct(product);

        FileDownloadProduct file = new FileDownloadProduct();
        file.setId(UUID.randomUUID());
        file.setFileName("f.txt");
        file.setSize(10);
        file.setFileType("text/plain");
        file.setPath("path/f.txt");
        file.setSection(section);

        section.setFiles(Set.of(file));
        product.setSectionDownloadProducts(Set.of(section));

        AbstractProductResponseDto dtoBase = converter.mapDownloadProductToResponse(product);
        assertTrue(dtoBase instanceof DownloadProductResponseDto);
        DownloadProductResponseDto dto = (DownloadProductResponseDto) dtoBase;
        assertEquals("free", dto.getPrice());
        assertEquals(1, dto.getSections().size());
        assertEquals("http://cdn/bucket/path/f.txt", dto.getSections().get(0).getFiles().get(0).getUrl());
    }
}
