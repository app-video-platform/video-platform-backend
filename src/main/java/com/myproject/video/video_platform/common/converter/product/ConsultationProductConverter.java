package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductDetailsDto;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductRequestDto;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductResponseDto;
import com.myproject.video.video_platform.entity.products.consultation.ConnectedCalendar;
import com.myproject.video.video_platform.entity.products.consultation.ConsultationProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.products.consultation.ConnectedCalendarRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConsultationProductConverter {

    private final ConnectedCalendarRepository calendarRepository;
    private static final ProductStatus DEFAULT_STATUS = ProductStatus.DRAFT;

    public ConsultationProductConverter(ConnectedCalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    /**
     * Map incoming DTO + user → JPA entity.
     */
    public ConsultationProduct fromDto(ConsultationProductRequestDto dto, User user) {
        ConsultationProduct entity = new ConsultationProduct();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(parseStatus(dto.getStatus(), DEFAULT_STATUS));
        entity.setPrice(parsePrice(dto.getPrice(), BigDecimal.ZERO));
        entity.setType(ProductType.CONSULTATION);
        entity.setUser(user);

        ConsultationProductDetailsDto details = dto.getDetails();
        if (details != null) {
            entity.setDurationMinutes(details.getDurationMinutes());
            entity.setMeetingMethod(details.getMeetingMethod() != null
                    ? ConsultationProduct.MeetingMethod.valueOf(details.getMeetingMethod().name())
                    : null);
            entity.setCustomLocation(details.getCustomLocation());
            entity.setBufferBeforeMinutes(details.getBufferBeforeMinutes());
            entity.setBufferAfterMinutes(details.getBufferAfterMinutes());
            entity.setMaxSessionsPerDay(details.getMaxSessionsPerDay());
            entity.setConfirmationMessage(details.getConfirmationMessage());
            entity.setCancellationPolicy(details.getCancellationPolicy());
        }

        return entity;
    }

    /**
     * Map JPA entity → outgoing DTO.
     */
    public ConsultationProductResponseDto toDto(ConsultationProduct entity) {
        List<ConnectedCalendar> calendars = calendarRepository.findAllByTeacherId((entity.getUser().getUserId()));

        ConsultationProductResponseDto dto = new ConsultationProductResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : DEFAULT_STATUS.name());
        dto.setPrice(entity.getPrice() == null
                ? "0"
                : entity.getPrice().compareTo(BigDecimal.ZERO) == 0
                ? "free"
                : entity.getPrice().toString());
        dto.setType(entity.getType() != null ? entity.getType().name() : ProductType.CONSULTATION.name());
        dto.setUserId(entity.getUser().getUserId());

        ConsultationProductDetailsDto details = new ConsultationProductDetailsDto();
        details.setDurationMinutes(entity.getDurationMinutes());
        details.setMeetingMethod(entity.getMeetingMethod() != null
                ? ConsultationProductDetailsDto.MeetingMethod.valueOf(entity.getMeetingMethod().name())
                : null);
        details.setCustomLocation(entity.getCustomLocation());
        details.setBufferBeforeMinutes(entity.getBufferBeforeMinutes());
        details.setBufferAfterMinutes(entity.getBufferAfterMinutes());
        details.setMaxSessionsPerDay(entity.getMaxSessionsPerDay());
        details.setConfirmationMessage(entity.getConfirmationMessage());
        details.setCancellationPolicy(entity.getCancellationPolicy());
        details.setConnectedCalendars(
                calendars.stream()
                        .map(this::toCalendarDto)
                        .collect(Collectors.toList())
        );

        dto.setDetails(details);
        return dto;
    }

    /**
     * Update an existing entity with fields from DTO.
     */
    public void updateEntityFromDto(ConsultationProductRequestDto dto, ConsultationProduct entity) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(parseStatus(dto.getStatus(), entity.getStatus() != null ? entity.getStatus() : DEFAULT_STATUS));
        }
        if (dto.getPrice() != null) {
            entity.setPrice(parsePrice(dto.getPrice(), entity.getPrice() != null ? entity.getPrice() : BigDecimal.ZERO));
        }

        ConsultationProductDetailsDto details = dto.getDetails();
        if (details == null) {
            return;
        }

        if (details.getDurationMinutes() != null) {
            entity.setDurationMinutes(details.getDurationMinutes());
        }
        if (details.getMeetingMethod() != null) {
            entity.setMeetingMethod(ConsultationProduct.MeetingMethod.valueOf(details.getMeetingMethod().name()));
        }
        if (details.getCustomLocation() != null) {
            entity.setCustomLocation(details.getCustomLocation());
        }
        if (details.getBufferBeforeMinutes() != null) {
            entity.setBufferBeforeMinutes(details.getBufferBeforeMinutes());
        }
        if (details.getBufferAfterMinutes() != null) {
            entity.setBufferAfterMinutes(details.getBufferAfterMinutes());
        }
        if (details.getMaxSessionsPerDay() != null) {
            entity.setMaxSessionsPerDay(details.getMaxSessionsPerDay());
        }
        if (details.getConfirmationMessage() != null) {
            entity.setConfirmationMessage(details.getConfirmationMessage());
        }
        if (details.getCancellationPolicy() != null) {
            entity.setCancellationPolicy(details.getCancellationPolicy());
        }
    }

    private ConsultationProductDetailsDto.ConnectedCalendarDto toCalendarDto(ConnectedCalendar cal) {
        ConsultationProductDetailsDto.ConnectedCalendarDto c = new ConsultationProductDetailsDto.ConnectedCalendarDto();
        c.setId(cal.getId().toString());
        c.setProvider(cal.getProvider().name());
        c.setExpiresAt(cal.getExpiresAt().toString());
        return c;
    }

    private ProductStatus parseStatus(String statusStr, ProductStatus fallback) {
        if (statusStr == null || statusStr.isBlank()) {
            return fallback;
        }
        try {
            return ProductStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private BigDecimal parsePrice(String priceStr, BigDecimal fallback) {
        if (priceStr == null || priceStr.isBlank()) {
            return fallback;
        }
        if (priceStr.equalsIgnoreCase("free")) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
