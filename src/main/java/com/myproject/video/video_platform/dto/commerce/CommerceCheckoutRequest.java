package com.myproject.video.video_platform.dto.commerce;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Products included in a one-time checkout.")
public class CommerceCheckoutRequest {

    @NotEmpty
    @Size(max = 20)
    @Schema(description = "Unique paid Product identifiers from one Creator.")
    private List<@NotNull UUID> productIds;
}
