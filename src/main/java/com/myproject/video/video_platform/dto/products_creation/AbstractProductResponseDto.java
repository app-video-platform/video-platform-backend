package com.myproject.video.video_platform.dto.products_creation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.myproject.video.video_platform.dto.products_creation.consultation.ConsultationProductResponseDto;
import com.myproject.video.video_platform.dto.products_creation.course.CourseProductResponseDto;
import com.myproject.video.video_platform.dto.products_creation.download.DownloadProductResponseDto;
import lombok.Data;

import java.util.UUID;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DownloadProductResponseDto.class, name = "DOWNLOAD"),
        @JsonSubTypes.Type(value = CourseProductResponseDto.class, name = "COURSE"),
        @JsonSubTypes.Type(value = ConsultationProductResponseDto.class, name = "CONSULTATION")
})
public abstract class AbstractProductResponseDto {
    private String type;         // "DOWNLOAD", "COURSE", "CONSULTATION"
    private UUID id;
    private String name;
    private String description;
    private String status;
    private String price;
    private UUID userId;
}
