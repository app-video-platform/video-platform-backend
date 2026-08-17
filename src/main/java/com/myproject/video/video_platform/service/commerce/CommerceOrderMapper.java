package com.myproject.video.video_platform.service.commerce;

import com.myproject.video.video_platform.dto.commerce.CommerceOrderItemResponse;
import com.myproject.video.video_platform.dto.commerce.CommerceOrderResponse;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.commerce.CommercePaymentAttempt;
import com.myproject.video.video_platform.repository.commerce.CommercePaymentAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class CommerceOrderMapper {

    private final CommercePaymentAttemptRepository paymentAttemptRepository;

    public CommerceOrderResponse toResponse(CommerceOrder order) {
        CommercePaymentAttempt payment = paymentAttemptRepository.findByOrderId(order.getId())
                .orElse(null);
        return new CommerceOrderResponse(
                order.getId(),
                order.getStatus(),
                payment != null ? payment.getProvider() : null,
                payment != null ? payment.getCheckoutUrl() : null,
                order.getCurrency(),
                order.getSubtotalMinor(),
                order.getTotalMinor(),
                order.getExpiresAt(),
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getRefundedAt(),
                order.getItems().stream()
                        .sorted(Comparator.comparing(item -> item.getProductId().toString()))
                        .map(item -> new CommerceOrderItemResponse(
                                item.getId(),
                                item.getProductId(),
                                item.getProductType(),
                                item.getProductName(),
                                item.getUnitAmountMinor(),
                                item.getQuantity(),
                                item.getLineTotalMinor()
                        ))
                        .toList()
        );
    }
}
