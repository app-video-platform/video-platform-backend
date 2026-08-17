package com.myproject.video.video_platform.dto.products.membership;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("MEMBERSHIP")
@Schema(description = "Payload for creating or updating a Membership Product.")
public class MembershipProductRequestDto extends AbstractProductRequestDto {
}
