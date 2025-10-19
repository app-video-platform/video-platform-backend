package com.myproject.video.video_platform.dto.products;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductRequestDto;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Abstract DTO with common product fields.
 * Polymorphic handling based on the "type" property in JSON.
 */
@Data
@Schema(
        description = "Base product mutation payload. Select the matching subtype via the type discriminator.",
        discriminatorProperty = "type",
        discriminatorMapping = {
                @DiscriminatorMapping(value = "DOWNLOAD", schema = DownloadProductRequestDto.class),
                @DiscriminatorMapping(value = "COURSE", schema = CourseProductRequestDto.class),
                @DiscriminatorMapping(value = "CONSULTATION", schema = ConsultationProductRequestDto.class)
        },
        oneOf = {DownloadProductRequestDto.class, CourseProductRequestDto.class, ConsultationProductRequestDto.class}
)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DownloadProductRequestDto.class, name = "DOWNLOAD"),
        @JsonSubTypes.Type(value = CourseProductRequestDto.class,   name = "COURSE"),
        @JsonSubTypes.Type(value = ConsultationProductRequestDto.class, name = "CONSULTATION")
})
public abstract class AbstractProductRequestDto {
    @Schema(description = "Product identifier for updates", example = "6f83c0cb-f8f4-4a6d-8e9a-bf5ac36b26be")
    private String id;
    @Schema(description = "Product kind discriminator", example = "COURSE")
    private String type;         // "DOWNLOAD", "COURSE", or "CONSULTATION" (upper-case)
    @Schema(description = "Customer-facing title", example = "Foundations of Lifestyle Photography")
    private String name;
    @Schema(description = "Short marketing description", example = "Step-by-step blueprint for building a paid photo coaching offer.")
    private String description;
    @Schema(description = "Lifecycle status", example = "DRAFT")
    private String status;       // "draft", "published", etc.
    @Schema(description = "Display price or 'free' token", example = "149.00")
    private String price;        // "free" or numeric string
    @Schema(description = "Owner user identifier", example = "738297f1-45fb-4f5f-98a5-6d0eb0a8f542")
    private String userId;
}
