package com.myproject.video.video_platform.service.commerce.payment;

import com.myproject.video.video_platform.common.enums.commerce.PaymentProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.commerce.provider", havingValue = "fake")
public class FakePaymentGateway implements PaymentGateway {

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.FAKE;
    }

    @Override
    public CheckoutGatewaySession createCheckoutSession(CheckoutGatewayCommand command) {
        return new CheckoutGatewaySession("fake_" + command.orderId(), null);
    }
}
