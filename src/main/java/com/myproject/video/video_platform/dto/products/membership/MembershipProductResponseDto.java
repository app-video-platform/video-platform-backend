package com.myproject.video.video_platform.dto.products.membership;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("MEMBERSHIP")
@Schema(description = "Membership Product view. Content and feed are returned by the Membership aggregate API.")
public class MembershipProductResponseDto extends AbstractProductResponseDto {
    @Schema(nullable = true, description = "Reserved for Product detail compatibility")
    private Object details;
}
