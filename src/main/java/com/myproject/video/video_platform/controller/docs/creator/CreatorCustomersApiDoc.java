package com.myproject.video.video_platform.controller.docs.creator;

import com.myproject.video.video_platform.dto.creator.customers.CreatorCustomerDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface CreatorCustomersApiDoc {
    @Operation(summary = "List Creator customers", description = "Returns buyers and users with free, purchased, or manually granted access to this Creator's Products.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Customers returned"), @ApiResponse(responseCode = "400", description = "Invalid filter"), @ApiResponse(responseCode = "403", description = "Creator role required")})
    ResponseEntity<CreatorCustomerDtos.Page> customers(int page, int pageSize, String search, String status, String product, String membership, String sort);

    @Operation(summary = "Get a Creator customer", description = "Returns Creator-scoped purchase, access, and recent activity history.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Customer returned"), @ApiResponse(responseCode = "403", description = "Creator role required"), @ApiResponse(responseCode = "404", description = "No relationship with this Creator")})
    ResponseEntity<CreatorCustomerDtos.Detail> customer(UUID customerId);
}
