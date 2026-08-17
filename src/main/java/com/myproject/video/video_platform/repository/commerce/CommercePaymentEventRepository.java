package com.myproject.video.video_platform.repository.commerce;

import com.myproject.video.video_platform.common.enums.commerce.PaymentProvider;
import com.myproject.video.video_platform.entity.commerce.CommercePaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommercePaymentEventRepository extends JpaRepository<CommercePaymentEvent, UUID> {

    boolean existsByProviderAndProviderEventId(PaymentProvider provider, String providerEventId);
}
