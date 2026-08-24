package com.myproject.video.video_platform.controller.creator;

import com.myproject.video.video_platform.controller.docs.creator.CreatorSalesApiDoc;
import com.myproject.video.video_platform.dto.creator.sales.CreatorSalesDtos;
import com.myproject.video.video_platform.service.creator.CreatorSalesService;
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
@RequestMapping("/api/creator")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
public class CreatorSalesController implements CreatorSalesApiDoc {
    private final CreatorSalesService salesService;

    @Override
    @GetMapping("/sales/summary")
    public ResponseEntity<CreatorSalesDtos.Summary> summary(@RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(salesService.summary(period));
    }

    @Override
    @GetMapping("/orders")
    public ResponseEntity<CreatorSalesDtos.OrdersPage> orders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) String product,
            @RequestParam(defaultValue = "30d") String period,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        return ResponseEntity.ok(salesService.orders(page, pageSize, search, status, product, period, sort));
    }

    @Override
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<CreatorSalesDtos.OrderDetail> order(@PathVariable UUID orderId) {
        return ResponseEntity.ok(salesService.order(orderId));
    }
}
