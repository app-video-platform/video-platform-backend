package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.ProductMinimised;
import com.myproject.video.video_platform.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@Tag(name = "Products", description = "Create and manage course, download, and consultation products for sale.")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Creates a draft product owned by the authenticated teacher. The type field selects the underlying product strategy.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or type",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the referenced product",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Referenced user not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AbstractProductResponseDto> createProduct(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Product definition including type-specific fields",
            required = true,
            content = @Content(schema = @Schema(implementation = AbstractProductRequestDto.class),
                    examples = @ExampleObject(name = "course",
                            value = "{\n  \"type\": \"COURSE\",\n  \"name\": \"Foundations of Lifestyle Photography\",\n  \"description\": \"Step-by-step blueprint...\",\n  \"status\": \"DRAFT\",\n  \"price\": \"149.00\",\n  \"userId\": \"738297f1-45fb-4f5f-98a5-6d0eb0a8f542\"\n}"))) @RequestBody AbstractProductRequestDto request) {
        log.info("Received create product request: {}", request);
        AbstractProductResponseDto response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List products for owner", description = "Returns all products owned by the supplied userId. Requires JWT belonging to that owner or an authorised role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<AbstractProductResponseDto>> getProducts(@RequestParam(name = "userId") String userId) {
        List<AbstractProductResponseDto> response = productService.getAllProductsForUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-products-min")
    @Operation(summary = "List products summary", description = "Returns condensed product cards either for all creators or a single user depending on userId parameter.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summaries returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductMinimised.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ProductMinimised>> getAllProductsMin(
            @RequestParam(name = "userId", required = false) String userId
    ) {
        List<ProductMinimised> response;
        if (userId != null) {
            response = productService.getAllProductsMinimisedForUser(userId);
        } else {
            response = productService.getAllProductsMinimised();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/getProduct")
    @Operation(summary = "Fetch product by id", description = "Retrieves a specific product by id and type, enforcing ownership access rules where applicable.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid type supplied",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AbstractProductResponseDto> getProductById(
            @RequestParam(name = "productId") String productId,
            @RequestParam(name = "type") String type) {

        AbstractProductResponseDto response = productService.getProductByIdAndType(productId, type);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    @Operation(summary = "Update product", description = "Updates product metadata based on the polymorphic payload. Only the owner may perform this operation.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Product updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or type",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the product",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AbstractProductResponseDto> updateProduct(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated product definition including type-specific fields",
            required = true,
            content = @Content(schema = @Schema(implementation = AbstractProductRequestDto.class))) @RequestBody AbstractProductRequestDto request) {
        log.info("Received update product request: {}", request.toString());
        AbstractProductResponseDto response = productService.updateProduct(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping()
    @Operation(summary = "Delete product", description = "Deletes a product soft record by invoking the type-specific strategy after checking ownership.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Product deleted",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = String.class),
                        examples = @ExampleObject(value = "\"Product deleted succesfully: 6f83c0cb-f8f4-4a6d-8e9a-bf5ac36b26be\""))),
        @ApiResponse(responseCode = "400", description = "Invalid product type",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "User does not own the product",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> deleteProduct(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "productType") String productType,
            @RequestParam(name = "id") String id) {
        log.info("Received delete product request: {}", id);
        productService.deleteProduct(userId, id, productType);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Product deleted succesfully: " + id);
    }

    /**
     * EXPLORE endpoint
     * GET /api/products/search?term=foo&page=0&size=20
     */
    @GetMapping("/search")
    @Operation(summary = "Search catalogue", description = "Returns a paginated list of public product summaries across all creators. Results are ordered by newest first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductMinimised.class)))
    })
    public Page<ProductMinimised> exploreSearch(
            @RequestParam String term,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return productService.searchAllProducts(term, pageable);
    }

    /**
     * LIBRARY / TEACHER endpoint
     * GET /api/users/{userId}/products?term=foo&page=0&size=20
     */
    @GetMapping("/search/{userId}/products")
    @Operation(summary = "Search owner library", description = "Finds products by name for the specified owner only. Requires authentication linked to the owner or appropriate privileges.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductMinimised.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Page<ProductMinimised> librarySearch(
            @PathVariable String userId,
            @RequestParam String term,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return productService.searchUserProducts(userId, term, pageable);
    }

}
