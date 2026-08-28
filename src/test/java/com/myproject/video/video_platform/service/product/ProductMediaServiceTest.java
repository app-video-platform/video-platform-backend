package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.entity.products.ProductMedia;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ProductMediaException;
import com.myproject.video.video_platform.repository.products.ProductMediaRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.service.digitalocean.SpacesS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductMediaServiceTest {
    private final ProductRepository products = mock(ProductRepository.class);
    private final ProductMediaRepository media = mock(ProductMediaRepository.class);
    private final ProductAuthorizationService authorization = mock(ProductAuthorizationService.class);
    private final SpacesS3Service spaces = mock(SpacesS3Service.class);
    private ProductMediaService service;
    private CourseProduct product;

    @BeforeEach
    void setUp() {
        service = new ProductMediaService(products, media, authorization, spaces,
                "https://cdn.example.com/", 10, 20, 2);
        User owner = new User(); owner.setUserId(UUID.randomUUID());
        product = new CourseProduct(); product.setId(UUID.randomUUID()); product.setUser(owner);
        when(products.findById(product.getId())).thenReturn(Optional.of(product));
        when(media.save(any(ProductMedia.class))).thenAnswer(invocation -> {
            ProductMedia item = invocation.getArgument(0);
            if (item.getId() == null) {
                var field = ProductMedia.class.getDeclaredField("id"); field.setAccessible(true); field.set(item, UUID.randomUUID());
            }
            return item;
        });
    }

    @Test
    void galleryUploadStreamsToOwnerScopedKeyAndReturnsReadyMetadata() {
        when(media.countByProductIdAndKind(product.getId(), ProductMedia.Kind.GALLERY_IMAGE)).thenReturn(0L);
        byte[] bytes = {1, 2, 3};
        var response = service.addGallery(product.getId(), new ByteArrayInputStream(bytes), bytes.length, "image/png");
        assertEquals("READY", response.getStatus()); assertEquals(0, response.getPosition());
        assertTrue(response.getUrl().startsWith("https://cdn.example.com/products/"));
        verify(authorization).requireOwnerOrAdmin(product);
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(spaces).upload(key.capture(), any(), eq(3L), eq("image/png"));
        assertTrue(key.getValue().contains(product.getId().toString()));
    }

    @Test
    void invalidTypeSizeAndGalleryLimitAreRejectedBeforeUpload() {
        assertThrows(ProductMediaException.class, () -> service.addGallery(product.getId(),
                new ByteArrayInputStream(new byte[1]), 1, "image/svg+xml"));
        assertThrows(ProductMediaException.class, () -> service.addGallery(product.getId(),
                new ByteArrayInputStream(new byte[11]), 11, "image/jpeg"));
        when(media.countByProductIdAndKind(product.getId(), ProductMedia.Kind.GALLERY_IMAGE)).thenReturn(2L);
        assertThrows(ProductMediaException.class, () -> service.addGallery(product.getId(),
                new ByteArrayInputStream(new byte[1]), 1, "image/webp"));
        verifyNoInteractions(spaces);
    }

    @Test
    void reorderRequiresCompleteDuplicateFreeSet() {
        ProductMedia first = gallery(0); ProductMedia second = gallery(1);
        when(media.findAllByProductIdAndKindOrderByGalleryPositionAsc(product.getId(), ProductMedia.Kind.GALLERY_IMAGE))
                .thenReturn(List.of(first, second));
        assertThrows(ProductMediaException.class, () -> service.reorder(product.getId(), List.of(first.getId(), first.getId())));
        var reordered = service.reorder(product.getId(), List.of(second.getId(), first.getId()));
        assertEquals(second.getId(), reordered.get(0).getId());
        assertEquals(0, second.getGalleryPosition()); assertEquals(1, first.getGalleryPosition());
    }

    @Test
    void cleanupRemovesDatabaseReferencesEvenWhenObjectDeletionFails() {
        ProductMedia item = gallery(0);
        when(media.findAllByProductId(product.getId())).thenReturn(List.of(item));
        doThrow(new RuntimeException("storage unavailable")).when(spaces).delete(item.getObjectKey());
        assertDoesNotThrow(() -> service.removeAll(product.getId()));
        verify(media).deleteAllByProductId(product.getId()); verify(media).flush();
    }

    private ProductMedia gallery(int position) {
        ProductMedia item = new ProductMedia();
        try { var field = ProductMedia.class.getDeclaredField("id"); field.setAccessible(true); field.set(item, UUID.randomUUID()); }
        catch (ReflectiveOperationException ex) { throw new RuntimeException(ex); }
        item.setProductId(product.getId()); item.setKind(ProductMedia.Kind.GALLERY_IMAGE); item.setObjectKey("key-" + position);
        item.setCdnUrl("https://cdn/item-" + position); item.setFileName("item.png"); item.setMimeType("image/png");
        item.setSize(1); item.setGalleryPosition(position); item.setStatus(ProductMedia.Status.READY); return item;
    }
}
