package com.myproject.video.video_platform.service.product;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.video.video_platform.common.converter.product.DownloadProductConverter;
import com.myproject.video.video_platform.common.converter.product.ProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductRequestDto;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.InvalidProductTypeException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.repository.products.download.DownloadProductRepository;
import com.myproject.video.video_platform.service.admin.AdminAuditService;
import com.myproject.video.video_platform.service.product.strategy_handler.ProductTypeHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceRoutingTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DownloadProductRepository downloadProductRepository;
    @Mock
    private DownloadProductConverter downloadProductConverter;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductConverter productConverter;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ProductAuthorizationService productAuthorizationService;
    @Mock
    private AdminAuditService adminAuditService;

    @Mock
    private ProductTypeHandler courseHandler;
    @Mock
    private ProductTypeHandler downloadHandler;
    @Mock
    private ProductTypeHandler consultationHandler;

    private ProductService service;

    @BeforeEach
    void setUp() {
        when(courseHandler.getSupportedType()).thenReturn(ProductType.COURSE);
        when(downloadHandler.getSupportedType()).thenReturn(ProductType.DOWNLOAD);
        when(consultationHandler.getSupportedType()).thenReturn(ProductType.CONSULTATION);

        service = new ProductService(
                userRepository,
                downloadProductRepository,
                downloadProductConverter,
                productRepository,
                Set.of(courseHandler, downloadHandler, consultationHandler),
                productConverter,
                objectMapper,
                productAuthorizationService,
                adminAuditService
        );

        Mockito.clearInvocations(courseHandler, downloadHandler, consultationHandler);
    }

    @Test
    void createProduct_routesToHandlerBasedOnType() {
        CourseProductRequestDto dto = new CourseProductRequestDto();
        dto.setType("COURSE");

        AbstractProductResponseDto response = Mockito.mock(AbstractProductResponseDto.class);
        when(courseHandler.createProduct(dto)).thenReturn(response);

        assertEquals(response, service.createProduct(dto));
        verify(courseHandler).createProduct(dto);
        verifyNoInteractions(downloadHandler, consultationHandler);
    }

    @Test
    void updateProduct_routesToHandlerBasedOnType() {
        DownloadProductRequestDto dto = new DownloadProductRequestDto();
        dto.setType("DOWNLOAD");
        dto.setId(UUID.randomUUID().toString());

        User user = new User();
        user.setUserId(UUID.randomUUID());
        DownloadProduct before = new DownloadProduct();
        before.setId(UUID.fromString(dto.getId()));
        before.setType(ProductType.DOWNLOAD);
        before.setName("Before");
        before.setUser(user);
        when(productRepository.findById(before.getId())).thenReturn(Optional.of(before));

        AbstractProductResponseDto response = Mockito.mock(AbstractProductResponseDto.class);
        when(downloadHandler.updateProduct(dto)).thenReturn(response);

        assertEquals(response, service.updateProduct(dto));
        verify(downloadHandler).updateProduct(dto);
        verifyNoInteractions(courseHandler, consultationHandler);
    }

    @Test
    void deleteProduct_routesToHandlerBasedOnType() {
        UUID productId = UUID.randomUUID();
        User user = new User();
        user.setUserId(UUID.randomUUID());
        DownloadProduct before = new DownloadProduct();
        before.setId(productId);
        before.setType(ProductType.CONSULTATION);
        before.setName("Before");
        before.setUser(user);
        when(productRepository.findById(productId)).thenReturn(Optional.of(before));

        service.deleteProduct("user", productId.toString(), "CONSULTATION");
        verify(consultationHandler).deleteProduct("user", productId.toString());
        verifyNoInteractions(courseHandler, downloadHandler);
    }

    @Test
    void getProductByIdAndType_invalidType_throws() {
        assertThrows(InvalidProductTypeException.class,
                () -> service.getProductByIdAndType(UUID.randomUUID().toString(), "not-a-type"));
    }

    @Test
    void getAllProductsForUser_fetchesEachProductWithTypeHandler() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        CourseProduct course = new CourseProduct();
        course.setId(UUID.randomUUID());
        course.setType(ProductType.COURSE);
        course.setUser(user);

        DownloadProduct download = new DownloadProduct();
        download.setId(UUID.randomUUID());
        download.setType(ProductType.DOWNLOAD);
        download.setUser(user);

        when(productRepository.findAllByUser(user)).thenReturn(List.of(course, download));

        AbstractProductResponseDto courseResp = Mockito.mock(AbstractProductResponseDto.class);
        AbstractProductResponseDto downloadResp = Mockito.mock(AbstractProductResponseDto.class);
        when(courseHandler.getProductById(course.getId().toString())).thenReturn(courseResp);
        when(downloadHandler.getProductById(download.getId().toString())).thenReturn(downloadResp);

        List<AbstractProductResponseDto> results = service.getAllProductsForUser(userId.toString());
        assertEquals(2, results.size());
        assertEquals(courseResp, results.get(0));
        assertEquals(downloadResp, results.get(1));

        verify(courseHandler).getProductById(course.getId().toString());
        verify(downloadHandler).getProductById(download.getId().toString());
        verifyNoInteractions(productConverter);
    }

    @Test
    void getProductById_routesToStoredProductTypeHandler() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);

        DownloadProduct download = new DownloadProduct();
        download.setId(UUID.randomUUID());
        download.setType(ProductType.DOWNLOAD);
        download.setUser(user);

        when(productRepository.findById(download.getId())).thenReturn(Optional.of(download));

        AbstractProductResponseDto response = Mockito.mock(AbstractProductResponseDto.class);
        when(downloadHandler.getProductById(download.getId().toString())).thenReturn(response);

        assertEquals(response, service.getProductById(download.getId().toString()));
        verify(downloadHandler).getProductById(download.getId().toString());
        verifyNoInteractions(courseHandler, consultationHandler);
    }

    @Test
    void deleteProductById_routesToStoredProductTypeHandler() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);

        DownloadProduct download = new DownloadProduct();
        download.setId(UUID.randomUUID());
        download.setType(ProductType.DOWNLOAD);
        download.setUser(user);

        when(productRepository.findById(download.getId())).thenReturn(Optional.of(download));

        service.deleteProductById(download.getId().toString());

        verify(downloadHandler).deleteProduct(userId.toString(), download.getId().toString());
        verifyNoInteractions(courseHandler, consultationHandler);
    }

    @Test
    void patchProduct_downloadPatchInjectsRoutingFieldsAndKeepsDetailsNull() {
        ProductService patchService = new ProductService(
                userRepository,
                downloadProductRepository,
                downloadProductConverter,
                productRepository,
                Set.of(courseHandler, downloadHandler, consultationHandler),
                productConverter,
                new ObjectMapper(),
                productAuthorizationService,
                adminAuditService
        );
        Mockito.clearInvocations(courseHandler, downloadHandler, consultationHandler);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);

        DownloadProduct download = new DownloadProduct();
        download.setId(UUID.randomUUID());
        download.setType(ProductType.DOWNLOAD);
        download.setUser(user);

        when(productRepository.findById(download.getId())).thenReturn(Optional.of(download));

        AbstractProductResponseDto response = Mockito.mock(AbstractProductResponseDto.class);
        ArgumentCaptor<DownloadProductRequestDto> captor = ArgumentCaptor.forClass(DownloadProductRequestDto.class);
        when(downloadHandler.updateProduct(captor.capture())).thenReturn(response);

        ObjectNode patch = new ObjectMapper().createObjectNode();
        patch.put("name", "Renamed download");

        assertEquals(response, patchService.patchProduct(download.getId().toString(), patch));

        DownloadProductRequestDto routedDto = captor.getValue();
        assertEquals(download.getId().toString(), routedDto.getId());
        assertEquals("DOWNLOAD", routedDto.getType());
        assertEquals(userId.toString(), routedDto.getUserId());
        assertEquals("Renamed download", routedDto.getName());
        assertNull(routedDto.getDetails());
        verifyNoInteractions(courseHandler, consultationHandler);
    }
}
