package com.myproject.video.video_platform.controller.docs.creator;

import com.myproject.video.video_platform.dto.creator.sales.CreatorSalesDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface CreatorSalesApiDoc {
    @Operation(summary = "Get Creator Sales summary", description = "Returns retained revenue, paid Orders, refunds, and failed payments for a UTC period.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Summary returned"), @ApiResponse(responseCode = "403", description = "Creator role required")})
    ResponseEntity<CreatorSalesDtos.Summary> summary(String period);

    @Operation(summary = "List Creator Orders", description = "Returns a filtered, Creator-scoped one-time Order ledger.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Orders returned"), @ApiResponse(responseCode = "400", description = "Invalid filter"), @ApiResponse(responseCode = "403", description = "Creator role required")})
    ResponseEntity<CreatorSalesDtos.OrdersPage> orders(int page, int pageSize, String search, String status, String product, String period, String sort);

    @Operation(summary = "Get a Creator Order", description = "Returns a Creator-owned Order with immutable item snapshots and entitlement-derived access.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Order returned"), @ApiResponse(responseCode = "403", description = "Creator role required"), @ApiResponse(responseCode = "404", description = "Order not found for this Creator")})
    ResponseEntity<CreatorSalesDtos.OrderDetail> order(UUID orderId);
}
