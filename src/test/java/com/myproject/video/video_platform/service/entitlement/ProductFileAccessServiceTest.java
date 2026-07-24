package com.myproject.video.video_platform.service.entitlement;

import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import com.myproject.video.video_platform.entity.products.download.FileDownloadProduct;
import com.myproject.video.video_platform.entity.products.download.SectionDownloadProduct;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.repository.products.download.FileDownloadProductRepository;
import com.myproject.video.video_platform.service.digitalocean.SpacesS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductFileAccessServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private FileDownloadProductRepository fileRepository;
    @Mock
    private ProductContentAccessService contentAccessService;
    @Mock
    private SpacesS3Service spacesS3Service;

    private ProductFileAccessService service;
    private DownloadProduct product;
    private FileDownloadProduct file;

    @BeforeEach
    void setUp() {
        service = new ProductFileAccessService(
                productRepository,
                fileRepository,
                contentAccessService,
                spacesS3Service
        );

        product = new DownloadProduct();
        product.setId(UUID.randomUUID());
        product.setType(ProductType.DOWNLOAD);

        SectionDownloadProduct section = new SectionDownloadProduct();
        section.setId(UUID.randomUUID());
        section.setDownloadProduct(product);

        file = new FileDownloadProduct();
        file.setId(UUID.randomUUID());
        file.setSection(section);
        file.setPath("private/file.zip");
    }

    @Test
    void accessIsRequiredBeforeGeneratingDownloadUrl() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        doThrow(new AccessDeniedException("required"))
                .when(contentAccessService).requireContentAccess(product);

        assertThrows(
                AccessDeniedException.class,
                () -> service.createDownloadUrl(product.getId(), file.getId())
        );
        verifyNoInteractions(fileRepository, spacesS3Service);
    }

    @Test
    void entitledDownloadGetsShortLivedSignedUrl() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(fileRepository.findById(file.getId())).thenReturn(Optional.of(file));
        when(spacesS3Service.generatePresignedUrlForGet(
                "private/file.zip",
                Duration.ofMinutes(10)
        )).thenReturn("https://signed.example/file");

        assertEquals(
                "https://signed.example/file",
                service.createDownloadUrl(product.getId(), file.getId())
        );
        verify(fileRepository).save(file);
        assertEquals(1, file.getDownloadCount());
    }
}
