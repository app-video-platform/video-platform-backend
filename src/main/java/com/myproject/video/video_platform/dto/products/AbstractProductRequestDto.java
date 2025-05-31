package com.myproject.video.video_platform.dto.products;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductRequestDto;
import lombok.Data;

/**
 * Abstract DTO with common product fields.
 * Polymorphic handling based on the "type" property in JSON.
 */
@Data
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
    private String id;
    private String type;         // "DOWNLOAD", "COURSE", or "CONSULTATION" (upper-case)
    private String name;
    private String description;
    private String status;       // "draft", "published", etc.
    private String price;        // "free" or numeric string
    private String userId;
}
