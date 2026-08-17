package com.myproject.video.video_platform.controller.docs.commerce;

import com.myproject.video.video_platform.dto.commerce.CommerceCheckoutRequest;
import com.myproject.video.video_platform.dto.commerce.CommerceOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface CommerceApiDoc {

    @Operation(
            summary = "Create a one-time checkout session",
            description = "Creates an idempotent checkout for published paid Products owned by one Creator. "
                    + "Prices and ownership are resolved by the server."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Checkout created or an existing idempotent checkout returned"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid idempotency key"),
            @ApiResponse(responseCode = "409", description = "Existing access or idempotency conflict"),
            @ApiResponse(responseCode = "422", description = "Invalid checkout contents"),
            @ApiResponse(responseCode = "503", description = "Paid checkout is not configured")
    })
    ResponseEntity<CommerceOrderResponse> createCheckout(
            CommerceCheckoutRequest request,
            @Parameter(description = "Client-generated key reused only for retries of the same checkout")
            String idempotencyKey
    );

    @Operation(
            summary = "Get checkout Order status",
            description = "Returns an Order to its buyer or an Administrator."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order returned"),
            @ApiResponse(responseCode = "403", description = "The caller does not own the Order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    ResponseEntity<CommerceOrderResponse> getOrder(UUID orderId);
}
