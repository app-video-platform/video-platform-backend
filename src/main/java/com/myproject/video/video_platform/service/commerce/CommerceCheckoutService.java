package com.myproject.video.video_platform.service.commerce;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.common.enums.commerce.PaymentAttemptStatus;
import com.myproject.video.video_platform.common.enums.entitlement.EntitlementStatus;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductPricingModel;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.commerce.CommerceCheckoutRequest;
import com.myproject.video.video_platform.dto.commerce.CommerceOrderResponse;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.commerce.CommerceOrderItem;
import com.myproject.video.video_platform.entity.commerce.CommercePaymentAttempt;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.commerce.CommerceException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.commerce.CommerceOrderRepository;
import com.myproject.video.video_platform.repository.commerce.CommercePaymentAttemptRepository;
import com.myproject.video.video_platform.repository.entitlement.ProductEntitlementRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.service.commerce.payment.CheckoutGatewayCommand;
import com.myproject.video.video_platform.service.commerce.payment.CheckoutGatewaySession;
import com.myproject.video.video_platform.service.commerce.payment.PaymentGateway;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class CommerceCheckoutService {

    private final CommerceOrderRepository orderRepository;
    private final CommercePaymentAttemptRepository paymentAttemptRepository;
    private final ProductRepository productRepository;
    private final ProductEntitlementRepository entitlementRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final CommerceOrderMapper orderMapper;
    private final PaymentGateway paymentGateway;
    private final boolean commerceEnabled;
    private final String currency;
    private final Duration sessionExpiry;

    public CommerceCheckoutService(
            CommerceOrderRepository orderRepository,
            CommercePaymentAttemptRepository paymentAttemptRepository,
            ProductRepository productRepository,
            ProductEntitlementRepository entitlementRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            CommerceOrderMapper orderMapper,
            ObjectProvider<PaymentGateway> paymentGatewayProvider,
            @Value("${app.commerce.enabled:false}") boolean commerceEnabled,
            @Value("${app.commerce.currency:EUR}") String currency,
            @Value("${app.commerce.session-expiry-minutes:30}") long sessionExpiryMinutes
    ) {
        this.orderRepository = orderRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.productRepository = productRepository;
        this.entitlementRepository = entitlementRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.orderMapper = orderMapper;
        this.paymentGateway = paymentGatewayProvider.getIfAvailable();
        this.commerceEnabled = commerceEnabled;
        String normalizedCurrency = currency == null
                ? ""
                : currency.trim().toUpperCase(Locale.ROOT);
        if (!"EUR".equals(normalizedCurrency)) {
            throw new IllegalArgumentException("Commerce currently supports EUR only");
        }
        if (sessionExpiryMinutes <= 0) {
            throw new IllegalArgumentException("Commerce session expiry must be positive");
        }
        this.currency = normalizedCurrency;
        this.sessionExpiry = Duration.ofMinutes(sessionExpiryMinutes);
    }

    @Transactional
    public CommerceOrderResponse createCheckout(
            CommerceCheckoutRequest request,
            String idempotencyKey
    ) {
        requireCommerceAvailable();
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        UUID buyerId = currentUserService.getCurrentUserId();
        List<UUID> productIds = request.getProductIds();
        requireUniqueProductIds(productIds);
        String fingerprint = fingerprint(productIds);

        // Serialize checkout creation per buyer so concurrent retries cannot create
        // duplicate orders before the idempotency-key uniqueness constraint is visible.
        User buyer = userRepository.findByIdForCommerceCheckout(buyerId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + buyerId));

        CommerceOrder existing = orderRepository
                .findByBuyerUserIdAndIdempotencyKey(buyerId, normalizedKey)
                .orElse(null);
        if (existing != null) {
            if (!existing.getCheckoutFingerprint().equals(fingerprint)) {
                throw new CommerceException(
                        HttpStatus.CONFLICT,
                        "The idempotency key was already used for a different checkout"
                );
            }
            expireIfNecessary(existing);
            return orderMapper.toResponse(existing);
        }

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw invalidCheckout("One or more Products do not exist");
        }

        User creator = validateProducts(products, buyerId);
        Instant expiresAt = Instant.now().plus(sessionExpiry);
        CommerceOrder order = new CommerceOrder();
        order.setBuyer(buyer);
        order.setCreator(creator);
        order.setStatus(CommerceOrderStatus.PENDING);
        order.setCurrency(currency);
        order.setIdempotencyKey(normalizedKey);
        order.setCheckoutFingerprint(fingerprint);
        order.setExpiresAt(expiresAt);

        long total = 0L;
        for (Product product : products) {
            long amountMinor = toMinorUnits(product.getPrice());
            total = addAmounts(total, amountMinor);
            CommerceOrderItem item = new CommerceOrderItem();
            item.setProductId(product.getId());
            item.setProductType(product.getType());
            item.setProductName(product.getName());
            item.setUnitAmountMinor(amountMinor);
            item.setQuantity(1);
            item.setLineTotalMinor(amountMinor);
            order.addItem(item);
        }
        order.setSubtotalMinor(total);
        order.setTotalMinor(total);
        order = orderRepository.saveAndFlush(order);

        CheckoutGatewaySession session = paymentGateway.createCheckoutSession(
                new CheckoutGatewayCommand(
                        order.getId(),
                        buyer.getEmail(),
                        total,
                        currency,
                        expiresAt
                )
        );
        validateGatewaySession(session);
        CommercePaymentAttempt attempt = new CommercePaymentAttempt();
        attempt.setOrder(order);
        attempt.setProvider(paymentGateway.provider());
        attempt.setStatus(PaymentAttemptStatus.PENDING);
        attempt.setProviderSessionId(session.providerSessionId());
        attempt.setCheckoutUrl(session.checkoutUrl());
        attempt.setAmountMinor(total);
        attempt.setCurrency(currency);
        paymentAttemptRepository.save(attempt);

        return orderMapper.toResponse(order);
    }

    private User validateProducts(List<Product> products, UUID buyerId) {
        User creator = null;
        for (Product product : products) {
            if (product.getType() == ProductType.MEMBERSHIP
                    || product.getPricingModel() == ProductPricingModel.RECURRING) {
                throw invalidCheckout("Recurring and Membership Products cannot use one-time checkout");
            }
            if (product.getStatus() != ProductStatus.PUBLISHED) {
                throw invalidCheckout("Only published Products can be purchased");
            }
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw invalidCheckout("Free Products must use the enrollment flow");
            }
            if (product.getUser().getUserId().equals(buyerId)) {
                throw invalidCheckout("Creators cannot purchase their own Products");
            }
            if (creator == null) {
                creator = product.getUser();
            } else if (!creator.getUserId().equals(product.getUser().getUserId())) {
                throw invalidCheckout("A checkout can contain Products from only one Creator");
            }
            if (entitlementRepository.existsByUserUserIdAndProductIdAndStatus(
                    buyerId,
                    product.getId(),
                    EntitlementStatus.ACTIVE
            )) {
                throw new CommerceException(
                        HttpStatus.CONFLICT,
                        "The buyer already has access to one or more Products"
                );
            }
            toMinorUnits(product.getPrice());
        }
        return creator;
    }

    private void requireCommerceAvailable() {
        if (!commerceEnabled || paymentGateway == null) {
            throw new CommerceException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Paid checkout is not configured"
            );
        }
    }

    private static String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 128) {
            throw new CommerceException(
                    HttpStatus.BAD_REQUEST,
                    "A valid Idempotency-Key header is required"
            );
        }
        return idempotencyKey.trim();
    }

    private static void requireUniqueProductIds(List<UUID> productIds) {
        Set<UUID> uniqueIds = new HashSet<>(productIds);
        if (uniqueIds.size() != productIds.size()) {
            throw invalidCheckout("Duplicate Products are not allowed in one checkout");
        }
    }

    private static long toMinorUnits(BigDecimal price) {
        try {
            return price.setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2)
                    .longValueExact();
        } catch (ArithmeticException ex) {
            throw invalidCheckout("Product prices must have at most two decimal places");
        }
    }

    private static long addAmounts(long total, long amountMinor) {
        try {
            return Math.addExact(total, amountMinor);
        } catch (ArithmeticException ex) {
            throw invalidCheckout("The checkout total is too large");
        }
    }

    private static void validateGatewaySession(CheckoutGatewaySession session) {
        if (session == null
                || session.providerSessionId() == null
                || session.providerSessionId().isBlank()
                || session.providerSessionId().length() > 255
                || (session.checkoutUrl() != null && session.checkoutUrl().length() > 2048)) {
            throw new CommerceException(
                    HttpStatus.BAD_GATEWAY,
                    "The payment provider returned an invalid checkout session"
            );
        }
    }

    private static String fingerprint(List<UUID> productIds) {
        String canonical = productIds.stream()
                .map(UUID::toString)
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElseThrow();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private void expireIfNecessary(CommerceOrder order) {
        if (order.getStatus() == CommerceOrderStatus.PENDING
                && order.getExpiresAt().isBefore(Instant.now())) {
            order.setStatus(CommerceOrderStatus.EXPIRED);
            paymentAttemptRepository.findByOrderId(order.getId()).ifPresent(attempt -> {
                attempt.setStatus(PaymentAttemptStatus.EXPIRED);
                paymentAttemptRepository.save(attempt);
            });
            orderRepository.save(order);
        }
    }

    private static CommerceException invalidCheckout(String message) {
        return new CommerceException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
