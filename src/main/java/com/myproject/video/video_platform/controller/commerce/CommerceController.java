package com.myproject.video.video_platform.controller.commerce;

import com.myproject.video.video_platform.controller.docs.commerce.CommerceApiDoc;
import com.myproject.video.video_platform.dto.commerce.CommerceCheckoutRequest;
import com.myproject.video.video_platform.dto.commerce.CommerceOrderResponse;
import com.myproject.video.video_platform.service.commerce.CommerceCheckoutService;
import com.myproject.video.video_platform.service.commerce.CommerceOrderQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/commerce")
@RequiredArgsConstructor
public class CommerceController implements CommerceApiDoc {

    private final CommerceCheckoutService checkoutService;
    private final CommerceOrderQueryService orderQueryService;

    @PostMapping("/checkout-sessions")
    @Override
    public ResponseEntity<CommerceOrderResponse> createCheckout(
            @Valid @RequestBody CommerceCheckoutRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkoutService.createCheckout(request, idempotencyKey));
    }

    @GetMapping("/orders/{orderId}")
    @Override
    public ResponseEntity<CommerceOrderResponse> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderQueryService.getOrder(orderId));
    }
}
