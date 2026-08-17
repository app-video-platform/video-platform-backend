package com.myproject.video.video_platform.dto.commerce;

import com.myproject.video.video_platform.common.enums.commerce.PaymentEventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FakePaymentSimulationRequest {

    @NotNull
    private PaymentEventType outcome;
}
