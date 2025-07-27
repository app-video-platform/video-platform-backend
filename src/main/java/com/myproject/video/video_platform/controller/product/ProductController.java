package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.service.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
        log.info("Received create product request: {}", request);
        AbstractProductResponseDto response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AbstractProductResponseDto>> getProducts(@RequestParam(name = "userId") String userId) {
        List<AbstractProductResponseDto> response = productService.getAllProductsForUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/getProduct")
    public ResponseEntity<AbstractProductResponseDto> getProductById(
            @RequestParam(name = "productId") String productId,
            @RequestParam(name = "type") String type) {

        AbstractProductResponseDto response = productService.getProductByIdAndType(productId, type);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<AbstractProductResponseDto> updateProduct(@RequestBody AbstractProductRequestDto request) {
        log.info("Received update product request: {}", request.toString());
        AbstractProductResponseDto response = productService.updateProduct(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteProduct(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "productType") String productType,
            @RequestParam(name = "id") String id) {
        log.info("Received delete product request: {}", id);
        productService.deleteProduct(userId, id, productType);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Product deleted succesfully: " + id);
    }

}
