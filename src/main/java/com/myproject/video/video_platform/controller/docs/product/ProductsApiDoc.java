package com.myproject.video.video_platform.controller.docs.product;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.ProductMinimised;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductsApiDoc {

    @Operation(
            summary = "Create product",
            description = "Creates a draft product owned by the authenticated teacher. The type field selects the underlying product strategy.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product definition including type-specific fields",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductRequestDto.class),
                            examples = @ExampleObject(
                                    name = "course",
                                    value = "{\n  \"type\": \"COURSE\",\n  \"name\": \"Foundations of Lifestyle Photography\",\n  \"description\": \"Step-by-step blueprint...\",\n  \"status\": \"DRAFT\",\n  \"price\": \"149.00\",\n  \"userId\": \"738297f1-45fb-4f5f-98a5-6d0eb0a8f542\"\n}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or type",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the referenced product",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Referenced user not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AbstractProductResponseDto> createProduct(AbstractProductRequestDto request);

    @Operation(
            summary = "List products for owner",
            description = "Returns all products owned by the supplied userId. Requires JWT belonging to that owner or an authorised role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<AbstractProductResponseDto>> getProducts(String userId);

    @Operation(
            summary = "List products summary",
            description = "Returns condensed product cards either for all creators or a single user depending on userId parameter."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summaries returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductMinimised.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<ProductMinimised>> getAllProductsMin(String userId);

    @Operation(
            summary = "Fetch product by id",
            description = "Retrieves a specific product by id and type, enforcing ownership access rules where applicable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid type supplied",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AbstractProductResponseDto> getProductById(String productId, String type);

    @Operation(
            summary = "Update product",
            description = "Updates product metadata based on the polymorphic payload. Only the owner may perform this operation.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated product definition including type-specific fields",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductRequestDto.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Product updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AbstractProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or type",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the product",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AbstractProductResponseDto> updateProduct(AbstractProductRequestDto request);

    @Operation(
            summary = "Delete product",
            description = "Deletes a product soft record by invoking the type-specific strategy after checking ownership."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Product deleted",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Product deleted succesfully: 6f83c0cb-f8f4-4a6d-8e9a-bf5ac36b26be\""))),
            @ApiResponse(responseCode = "400", description = "Invalid product type",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the product",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<String> deleteProduct(String userId, String productType, String id);

    @Operation(
            summary = "Search catalogue",
            description = "Returns a paginated list of public product summaries across all creators. Results are ordered by newest first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductMinimised.class)))
    })
    Page<ProductMinimised> exploreSearch(String term, Pageable pageable);

    @Operation(
            summary = "Search owner library",
            description = "Finds products by name for the specified owner only. Requires authentication linked to the owner or appropriate privileges."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductMinimised.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    Page<ProductMinimised> librarySearch(String userId, String term, Pageable pageable);
}

