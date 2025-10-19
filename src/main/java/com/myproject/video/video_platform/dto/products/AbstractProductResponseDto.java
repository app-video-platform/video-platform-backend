package com.myproject.video.video_platform.dto.products;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductResponseDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductResponseDto;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(
        description = "Base product view returned by the API. Concrete shape depends on type.",
        discriminatorProperty = "type",
        discriminatorMapping = {
                @DiscriminatorMapping(value = "DOWNLOAD", schema = DownloadProductResponseDto.class),
                @DiscriminatorMapping(value = "COURSE", schema = CourseProductResponseDto.class),
                @DiscriminatorMapping(value = "CONSULTATION", schema = ConsultationProductResponseDto.class)
        },
        oneOf = {DownloadProductResponseDto.class, CourseProductResponseDto.class, ConsultationProductResponseDto.class}
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DownloadProductResponseDto.class, name = "DOWNLOAD"),
        @JsonSubTypes.Type(value = CourseProductResponseDto.class, name = "COURSE"),
        @JsonSubTypes.Type(value = ConsultationProductResponseDto.class, name = "CONSULTATION")
})
public abstract class AbstractProductResponseDto {
    @Schema(description = "Product kind discriminator", example = "COURSE")
    private String type;         // "DOWNLOAD", "COURSE", "CONSULTATION"
    @Schema(description = "Product identifier", example = "a8c5d4a9-dc93-4c71-9c33-5d56f3d6b21d")
    private UUID id;
    @Schema(description = "Customer-facing title", example = "Foundations of Lifestyle Photography")
    private String name;
    @Schema(description = "Public description shown on storefront", example = "Learn how to script and record bingeable coaching videos in under two weeks.")
    private String description;
    @Schema(description = "Lifecycle status", example = "PUBLISHED")
    private String status;
    @Schema(description = "Display price or 'free' token", example = "149.00")
    private String price;
    @Schema(description = "Owner user identifier", example = "738297f1-45fb-4f5f-98a5-6d0eb0a8f542")
    private UUID userId;
}
