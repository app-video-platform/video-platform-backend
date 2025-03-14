package com.myproject.video.video_platform.dto.products_creation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * Abstract DTO with common product fields.
 * Polymorphic handling based on the "type" property in JSON.
 */
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DownloadProductRequestDto.class, name = "DOWNLOAD"),
        @JsonSubTypes.Type(value = CourseProductRequestDto.class,   name = "COURSE"),
        @JsonSubTypes.Type(value = ConsultationProductRequestDto.class, name = "CONSULTATION")
})
public abstract class AbstractProductRequestDto {
    private String type;         // "DOWNLOAD", "COURSE", or "CONSULTATION" (upper-case)
    private String name;
    private String description;
    private String status;       // "draft", "published", etc.
    private String price;        // "free" or numeric string
    private String userId;
}
