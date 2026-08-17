package com.myproject.video.video_platform.service.commerce;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.common.enums.commerce.PaymentAttemptStatus;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.commerce.CommerceOrderItem;
import com.myproject.video.video_platform.entity.commerce.CommercePaymentAttempt;
import com.myproject.video.video_platform.entity.commerce.CommercePaymentEvent;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.exception.commerce.CommerceException;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.commerce.CommerceOrderRepository;
import com.myproject.video.video_platform.repository.commerce.CommercePaymentAttemptRepository;
import com.myproject.video.video_platform.repository.commerce.CommercePaymentEventRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.service.commerce.payment.NormalizedPaymentEvent;
import com.myproject.video.video_platform.service.entitlement.ProductEntitlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CommercePaymentEventService {

    private final CommerceOrderRepository orderRepository;
    private final CommercePaymentAttemptRepository paymentAttemptRepository;
    private final CommercePaymentEventRepository paymentEventRepository;
    private final ProductRepository productRepository;
    private final ProductEntitlementService entitlementService;

    @Transactional
    public void process(NormalizedPaymentEvent event) {
        CommerceOrder order = orderRepository.findDetailedByIdForUpdate(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commerce Order not found: " + event.orderId()
                ));
        if (paymentEventRepository.existsByProviderAndProviderEventId(
                event.provider(),
                event.providerEventId()
        )) {
            return;
        }
        CommercePaymentAttempt attempt = paymentAttemptRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CommerceException(
                        HttpStatus.CONFLICT,
                        "The Order has no payment attempt"
                ));
        if (attempt.getProvider() != event.provider()) {
            throw new CommerceException(
                    HttpStatus.CONFLICT,
                    "Payment provider does not match the Order"
            );
        }
        if (attempt.getAmountMinor() != event.amountMinor()
                || !attempt.getCurrency().equalsIgnoreCase(event.currency())) {
            throw new CommerceException(
                    HttpStatus.CONFLICT,
                    "Payment amount or currency does not match the Order"
            );
        }

        switch (event.eventType()) {
            case PAID -> markPaid(order, attempt, event.providerPaymentId());
            case FAILED -> markFailed(order, attempt);
            case REFUNDED -> markRefunded(order, attempt);
        }

        CommercePaymentEvent processed = new CommercePaymentEvent();
        processed.setProvider(event.provider());
        processed.setProviderEventId(event.providerEventId());
        processed.setOrderId(order.getId());
        processed.setEventType(event.eventType());
        paymentEventRepository.save(processed);
    }

    private void markPaid(
            CommerceOrder order,
            CommercePaymentAttempt attempt,
            String providerPaymentId
    ) {
        if (order.getStatus() == CommerceOrderStatus.PAID) {
            return;
        }
        requireStatus(order, CommerceOrderStatus.PENDING, "paid");

        for (CommerceOrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Purchased Product not found: " + item.getProductId()
                    ));
            entitlementService.grantPurchase(
                    order.getBuyer().getUserId(),
                    product,
                    item.getId()
            );
        }

        Instant now = Instant.now();
        order.setStatus(CommerceOrderStatus.PAID);
        order.setPaidAt(now);
        attempt.setStatus(PaymentAttemptStatus.SUCCEEDED);
        attempt.setProviderPaymentId(providerPaymentId);
        paymentAttemptRepository.save(attempt);
        orderRepository.save(order);
    }

    private void markFailed(CommerceOrder order, CommercePaymentAttempt attempt) {
        if (order.getStatus() == CommerceOrderStatus.FAILED) {
            return;
        }
        requireStatus(order, CommerceOrderStatus.PENDING, "failed");
        order.setStatus(CommerceOrderStatus.FAILED);
        order.setFailedAt(Instant.now());
        attempt.setStatus(PaymentAttemptStatus.FAILED);
        attempt.setFailureCode("PAYMENT_FAILED");
        attempt.setFailureMessage("The payment provider reported a failed payment");
        paymentAttemptRepository.save(attempt);
        orderRepository.save(order);
    }

    private void markRefunded(CommerceOrder order, CommercePaymentAttempt attempt) {
        if (order.getStatus() == CommerceOrderStatus.REFUNDED) {
            return;
        }
        requireStatus(order, CommerceOrderStatus.PAID, "refunded");
        for (CommerceOrderItem item : order.getItems()) {
            entitlementService.revokePurchase(
                    order.getBuyer().getUserId(),
                    item.getProductId(),
                    item.getId()
            );
        }
        order.setStatus(CommerceOrderStatus.REFUNDED);
        order.setRefundedAt(Instant.now());
        attempt.setStatus(PaymentAttemptStatus.REFUNDED);
        paymentAttemptRepository.save(attempt);
        orderRepository.save(order);
    }

    private static void requireStatus(
            CommerceOrder order,
            CommerceOrderStatus expected,
            String transition
    ) {
        if (order.getStatus() != expected) {
            throw new CommerceException(
                    HttpStatus.CONFLICT,
                    "Order in status " + order.getStatus() + " cannot be marked " + transition
            );
        }
    }
}
