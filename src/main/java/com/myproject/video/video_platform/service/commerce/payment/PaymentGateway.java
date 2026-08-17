package com.myproject.video.video_platform.service.commerce.payment;

import com.myproject.video.video_platform.common.enums.commerce.PaymentProvider;

public interface PaymentGateway {

    PaymentProvider provider();

    CheckoutGatewaySession createCheckoutSession(CheckoutGatewayCommand command);
}
