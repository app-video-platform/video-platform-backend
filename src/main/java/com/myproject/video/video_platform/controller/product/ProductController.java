package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.dto.products_creation.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductResponseDto;
import com.myproject.video.video_platform.service.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<AbstractProductResponseDto> createProduct(@RequestBody AbstractProductRequestDto request) {
        log.info("Received createProduct request: {}", request);
        AbstractProductResponseDto response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AbstractProductResponseDto>> getProducts(@RequestParam(name = "userId") String userId) {
        List<AbstractProductResponseDto> response = productService.getAllDownloadProductsForUser(userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
