package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.download.DownloadProductDetailsRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductDetailsResponseDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductResponseDto;
import com.myproject.video.video_platform.dto.products.download.SectionDownloadProductRequestDto;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import com.myproject.video.video_platform.entity.products.download.FileDownloadProduct;
import com.myproject.video.video_platform.entity.products.download.SectionDownloadProduct;
import com.myproject.video.video_platform.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadProductConverterTest {

    private DownloadProductConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DownloadProductConverter();
        ReflectionTestUtils.setField(converter, "cdnEndpoint", "https://cdn.example.com");
        ReflectionTestUtils.setField(converter, "bucketName", "bucket");
    }

    @Test
    void mapDownloadProductRequestDtoToEntity_detailsNull_producesNoSections() {
        User owner = new User();
        owner.setUserId(UUID.randomUUID());

        DownloadProductRequestDto dto = new DownloadProductRequestDto();
        dto.setName("Download");
        dto.setDescription("Desc");
        dto.setStatus(null);
        dto.setPrice("free");
        dto.setDetails(null);

        DownloadProduct product = converter.mapDownloadProductRequestDtoToEntity(dto, owner);

        assertEquals(ProductType.DOWNLOAD, product.getType());
        assertEquals(ProductStatus.DRAFT, product.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(product.getPrice()));
        assertSame(owner, product.getUser());
        assertNotNull(product.getSectionDownloadProducts());
        assertEquals(0, product.getSectionDownloadProducts().size());
    }

    @Test
    void mapDownloadProductRequestDtoToEntity_withDetails_linksSectionsBackToProduct() {
        User owner = new User();
        owner.setUserId(UUID.randomUUID());

        SectionDownloadProductRequestDto sectionDto = new SectionDownloadProductRequestDto();
        sectionDto.setTitle("Section");
        sectionDto.setDescription("S desc");
        sectionDto.setPosition(1);
        DownloadProductDetailsRequestDto details = new DownloadProductDetailsRequestDto();
        details.setSections(List.of(sectionDto));

        DownloadProductRequestDto dto = new DownloadProductRequestDto();
        dto.setName("Download");
        dto.setDescription("Desc");
        dto.setStatus("DRAFT");
        dto.setPrice("12.50");
        dto.setDetails(details);

        DownloadProduct product = converter.mapDownloadProductRequestDtoToEntity(dto, owner);
        assertEquals(1, product.getSectionDownloadProducts().size());
        SectionDownloadProduct section = product.getSectionDownloadProducts().iterator().next();
        assertEquals("Section", section.getTitle());
        assertEquals("S desc", section.getDescription());
        assertEquals(1, section.getPosition());
        assertSame(product, section.getDownloadProduct());
    }

    @Test
    void mapDownloadProductUpdate_detailsNull_doesNotChangeSections() {
        DownloadProduct product = new DownloadProduct();
        product.setType(ProductType.DOWNLOAD);
        product.setStatus(ProductStatus.PUBLISHED);
        product.setName("Old");
        product.setDescription("Old desc");
        product.setPrice(BigDecimal.TEN);

        SectionDownloadProduct existingSection = new SectionDownloadProduct();
        existingSection.setId(UUID.randomUUID());
        existingSection.setTitle("Existing");
        existingSection.setDownloadProduct(product);
        product.getSectionDownloadProducts().add(existingSection);

        DownloadProductRequestDto dto = new DownloadProductRequestDto();
        dto.setName("New");
        dto.setDetails(null);

        converter.mapDownloadProductUpdate(product, dto);

        assertEquals("New", product.getName());
        assertEquals(1, product.getSectionDownloadProducts().size());
        assertEquals("Existing", product.getSectionDownloadProducts().iterator().next().getTitle());
    }

    @Test
    void mapDownloadProductUpdate_updatesExistingAddsNewAndRemovesMissing() {
        DownloadProduct product = new DownloadProduct();
        product.setType(ProductType.DOWNLOAD);

        SectionDownloadProduct sectionA = new SectionDownloadProduct();
        sectionA.setId(UUID.randomUUID());
        sectionA.setTitle("A");
        sectionA.setDownloadProduct(product);

        SectionDownloadProduct sectionB = new SectionDownloadProduct();
        sectionB.setId(UUID.randomUUID());
        sectionB.setTitle("B");
        sectionB.setDownloadProduct(product);

        product.getSectionDownloadProducts().addAll(Set.of(sectionA, sectionB));

        SectionDownloadProductRequestDto aUpdate = new SectionDownloadProductRequestDto();
        aUpdate.setId(sectionA.getId().toString());
        aUpdate.setTitle("A2");
        aUpdate.setDescription("updated");
        aUpdate.setPosition(1);

        SectionDownloadProductRequestDto sectionC = new SectionDownloadProductRequestDto();
        sectionC.setTitle("C");
        sectionC.setPosition(2);

        DownloadProductDetailsRequestDto details = new DownloadProductDetailsRequestDto();
        details.setSections(List.of(aUpdate, sectionC));

        DownloadProductRequestDto dto = new DownloadProductRequestDto();
        dto.setDetails(details);

        converter.mapDownloadProductUpdate(product, dto);

        assertEquals(2, product.getSectionDownloadProducts().size());
        assertTrue(product.getSectionDownloadProducts().stream().anyMatch(sec -> "A2".equals(sec.getTitle())));
        assertTrue(product.getSectionDownloadProducts().stream().anyMatch(sec -> "C".equals(sec.getTitle())));
        assertTrue(product.getSectionDownloadProducts().stream().noneMatch(sec -> "B".equals(sec.getTitle())));
        product.getSectionDownloadProducts().forEach(sec -> assertSame(product, sec.getDownloadProduct()));
    }

    @Test
    void mapDownloadProductToResponse_putsSectionsUnderDetailsAndBuildsFileUrls() {
        User owner = new User();
        owner.setUserId(UUID.randomUUID());

        DownloadProduct product = new DownloadProduct();
        product.setId(UUID.randomUUID());
        product.setType(ProductType.DOWNLOAD);
        product.setStatus(ProductStatus.PUBLISHED);
        product.setName("Download");
        product.setDescription("Desc");
        product.setPrice(BigDecimal.valueOf(5));
        product.setUser(owner);

        SectionDownloadProduct section = new SectionDownloadProduct();
        section.setId(UUID.randomUUID());
        section.setTitle("Section");
        section.setPosition(1);
        section.setDownloadProduct(product);

        FileDownloadProduct file = new FileDownloadProduct();
        file.setId(UUID.randomUUID());
        file.setFileName("file.zip");
        file.setPath("users-content/u/section/file.zip");
        file.setFileType("application/zip");
        file.setSize(123);
        file.setUploadedAt(LocalDateTime.now());
        file.setSection(section);
        section.setFiles(Set.of(file));

        product.getSectionDownloadProducts().add(section);

        DownloadProductResponseDto dto = (DownloadProductResponseDto) converter.mapDownloadProductToResponse(product);
        assertEquals("DOWNLOAD", dto.getType());
        DownloadProductDetailsResponseDto details = dto.getDetails();
        assertNotNull(details);
        assertEquals(1, details.getSections().size());
        assertEquals(1, details.getSections().get(0).getFiles().size());
        assertEquals("https://cdn.example.com/bucket/users-content/u/section/file.zip",
                details.getSections().get(0).getFiles().get(0).getUrl());
    }
}

