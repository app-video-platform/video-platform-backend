package com.myproject.video.video_platform.service.commerce;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.common.enums.commerce.PaymentAttemptStatus;
import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.commerce.CommerceOrderResponse;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.commerce.CommerceOrderRepository;
import com.myproject.video.video_platform.repository.commerce.CommercePaymentAttemptRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommerceOrderQueryService {

    private final CommerceOrderRepository orderRepository;
    private final CommercePaymentAttemptRepository paymentAttemptRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final CommerceOrderMapper orderMapper;

    @Transactional
    public CommerceOrderResponse getOrder(UUID orderId) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUserId));
        CommerceOrder order = orderRepository.findDetailedByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commerce Order not found: " + orderId
                ));
        boolean isBuyer = order.getBuyer().getUserId().equals(currentUserId);
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(UserRole.ADMIN.name()));
        if (!isBuyer && !isAdmin) {
            throw new AccessDeniedException("The current user cannot inspect this Order");
        }
        expireIfNecessary(order);
        return orderMapper.toResponse(order);
    }

    private void expireIfNecessary(CommerceOrder order) {
        if (order.getStatus() != CommerceOrderStatus.PENDING
                || !order.getExpiresAt().isBefore(Instant.now())) {
            return;
        }
        order.setStatus(CommerceOrderStatus.EXPIRED);
        paymentAttemptRepository.findByOrderId(order.getId()).ifPresent(attempt -> {
            attempt.setStatus(PaymentAttemptStatus.EXPIRED);
            paymentAttemptRepository.save(attempt);
        });
        orderRepository.save(order);
    }
}
