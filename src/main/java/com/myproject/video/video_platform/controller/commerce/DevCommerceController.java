package com.myproject.video.video_platform.controller.commerce;

import com.myproject.video.video_platform.common.enums.commerce.PaymentProvider;
import com.myproject.video.video_platform.dto.commerce.CommerceOrderResponse;
import com.myproject.video.video_platform.dto.commerce.FakePaymentSimulationRequest;
import com.myproject.video.video_platform.service.commerce.CommerceOrderQueryService;
import com.myproject.video.video_platform.service.commerce.CommercePaymentEventService;
import com.myproject.video.video_platform.service.commerce.payment.NormalizedPaymentEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dev/commerce")
@RequiredArgsConstructor
@Profile({"dev", "test"})
@ConditionalOnProperty(name = "app.commerce.fake.enabled", havingValue = "true")
public class DevCommerceController {

    private final CommercePaymentEventService paymentEventService;
    private final CommerceOrderQueryService orderQueryService;

    @PostMapping("/orders/{orderId}/simulate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommerceOrderResponse> simulate(
            @PathVariable UUID orderId,
            @Valid @RequestBody FakePaymentSimulationRequest request
    ) {
        CommerceOrderResponse currentOrder = orderQueryService.getOrder(orderId);
        paymentEventService.process(new NormalizedPaymentEvent(
                PaymentProvider.FAKE,
                "fake-event-" + UUID.randomUUID(),
                orderId,
                request.getOutcome(),
                request.getOutcome().name().equals("PAID") ? "fake-payment-" + orderId : null,
                currentOrder.totalMinor(),
                currentOrder.currency()
        ));
        return ResponseEntity.ok(orderQueryService.getOrder(orderId));
    }
}
