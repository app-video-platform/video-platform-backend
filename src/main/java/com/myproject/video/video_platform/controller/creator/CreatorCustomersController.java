package com.myproject.video.video_platform.controller.creator;

import com.myproject.video.video_platform.controller.docs.creator.CreatorCustomersApiDoc;
import com.myproject.video.video_platform.dto.creator.customers.CreatorCustomerDtos;
import com.myproject.video.video_platform.service.creator.CreatorCustomersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/creator/customers")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
public class CreatorCustomersController implements CreatorCustomersApiDoc {
    private final CreatorCustomersService customersService;

    @Override
    @GetMapping
    public ResponseEntity<CreatorCustomerDtos.Page> customers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) String product,
            @RequestParam(defaultValue = "all") String membership,
            @RequestParam(defaultValue = "last-activity-desc") String sort
    ) {
        return ResponseEntity.ok(customersService.customers(page, pageSize, search, status, product, membership, sort));
    }

    @Override
    @GetMapping("/{customerId}")
    public ResponseEntity<CreatorCustomerDtos.Detail> customer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(customersService.customer(customerId));
    }
}
