package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
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
        entity.setStatus(ProductStatus.valueOf(dto.getStatus()));
        entity.setPrice(
                dto.getPrice() == null
                        ? BigDecimal.ZERO
                        : dto.getPrice().equalsIgnoreCase("free")
                        ? BigDecimal.ZERO
                        : new BigDecimal(dto.getPrice())
        );
        entity.setType(ProductType.CONSULTATION);
        entity.setUser(user);
        entity.setDurationMinutes(dto.getDurationMinutes());
        entity.setMeetingMethod(ConsultationProduct.MeetingMethod.valueOf(dto.getMeetingMethod().name()));
        entity.setCustomLocation(dto.getCustomLocation());
        entity.setBufferBeforeMinutes(dto.getBufferBeforeMinutes());
        entity.setBufferAfterMinutes(dto.getBufferAfterMinutes());
        entity.setMaxSessionsPerDay(dto.getMaxSessionsPerDay());
        entity.setConfirmationMessage(dto.getConfirmationMessage());
        entity.setCancellationPolicy(dto.getCancellationPolicy());
        return entity;
    }

    /**
     * Map JPA entity → outgoing DTO.
     */
    public ConsultationProductResponseDto toDto(ConsultationProduct entity) {
        List<ConnectedCalendar> calendars = calendarRepository.findAllByTeacherId((entity.getUser().getUserId()));

        ConsultationProductResponseDto dto = new ConsultationProductResponseDto();
        dto.setId(entity.getId());
        dto.setType("CONSULTATION");
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(String.valueOf(entity.getStatus()));
        dto.setPrice(
                entity.getPrice() == null
                        ? "0"
                        : entity.getPrice().compareTo(BigDecimal.ZERO) == 0
                        ? "free"
                        : entity.getPrice().toString()
        );
        dto.setType(String.valueOf(entity.getType()));
        dto.setUserId(entity.getUser().getUserId());

        dto.setDurationMinutes(entity.getDurationMinutes());
        dto.setMeetingMethod(
                ConsultationProductResponseDto.MeetingMethod.valueOf(entity.getMeetingMethod().name())
        );
        dto.setCustomLocation(entity.getCustomLocation());
        dto.setBufferBeforeMinutes(entity.getBufferBeforeMinutes());
        dto.setBufferAfterMinutes(entity.getBufferAfterMinutes());
        dto.setMaxSessionsPerDay(entity.getMaxSessionsPerDay());
        dto.setConfirmationMessage(entity.getConfirmationMessage());
        dto.setCancellationPolicy(entity.getCancellationPolicy());

        dto.setConnectedCalendars(
                calendars.stream()
                        .map(this::toCalendarDto)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    /**
     * Update an existing entity with fields from DTO.
     */
    public void updateEntityFromDto(ConsultationProductRequestDto dto, ConsultationProduct entity) {
        // inherited
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(ProductStatus.valueOf(dto.getStatus()));
        entity.setPrice(
                dto.getPrice() == null
                        ? BigDecimal.ZERO
                        : dto.getPrice().equalsIgnoreCase("free")
                        ? BigDecimal.ZERO
                        : new BigDecimal(dto.getPrice())
        );

        // consultation-specific
        entity.setDurationMinutes(dto.getDurationMinutes());
        entity.setMeetingMethod(ConsultationProduct.MeetingMethod.valueOf(dto.getMeetingMethod().name()));
        entity.setCustomLocation(dto.getCustomLocation());
        entity.setBufferBeforeMinutes(dto.getBufferBeforeMinutes());
        entity.setBufferAfterMinutes(dto.getBufferAfterMinutes());
        entity.setMaxSessionsPerDay(dto.getMaxSessionsPerDay());
        entity.setConfirmationMessage(dto.getConfirmationMessage());
        entity.setCancellationPolicy(dto.getCancellationPolicy());
    }

    private ConsultationProductResponseDto.ConnectedCalendarDto toCalendarDto(ConnectedCalendar cal) {
        ConsultationProductResponseDto.ConnectedCalendarDto c = new ConsultationProductResponseDto.ConnectedCalendarDto();
        c.setId(cal.getId().toString());
        c.setProvider(cal.getProvider().name());
        c.setExpiresAt(cal.getExpiresAt().toString());
        return c;
    }
}
