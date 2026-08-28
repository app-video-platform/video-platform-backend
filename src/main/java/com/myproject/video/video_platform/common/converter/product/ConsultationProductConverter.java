package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductDetailsDto;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductRequestDto;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductResponseDto;
import com.myproject.video.video_platform.entity.products.consultation.ConnectedCalendar;
import com.myproject.video.video_platform.entity.products.consultation.ConsultationAvailabilityDay;
import com.myproject.video.video_platform.entity.products.consultation.ConsultationAvailabilityWindow;
import com.myproject.video.video_platform.entity.products.consultation.ConsultationProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.products.consultation.ConnectedCalendarRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        ProductPricingSupport.initializeOneTime(entity);
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
            applyAvailability(entity, details.getWeeklyAvailability());
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
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : entity.getCreatedAt());
        ProductPricingSupport.mapResponse(entity, dto);

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
        details.setWeeklyAvailability(toAvailabilityDto(entity));

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
        if (details.getWeeklyAvailability() != null) {
            applyAvailability(entity, details.getWeeklyAvailability());
        }
    }

    private void applyAvailability(ConsultationProduct entity,
                                   List<ConsultationProductDetailsDto.AvailabilityDayDto> requested) {
        if (requested == null) return;
        Set<ConsultationProductDetailsDto.Weekday> seen = new HashSet<>();
        List<ConsultationAvailabilityDay> replacement = new java.util.ArrayList<>();
        for (ConsultationProductDetailsDto.AvailabilityDayDto source : requested) {
            if (source == null || source.getDay() == null || !seen.add(source.getDay())) {
                throw new IllegalArgumentException("Weekly availability contains a missing or duplicate weekday");
            }
            ConsultationAvailabilityDay day = new ConsultationAvailabilityDay();
            day.setConsultation(entity);
            day.setWeekday(ConsultationAvailabilityDay.Weekday.valueOf(source.getDay().name()));
            day.setEnabled(Boolean.TRUE.equals(source.getEnabled()));
            List<ConsultationProductDetailsDto.AvailabilityWindowDto> slots = source.getWindows() == null
                    ? List.of() : source.getWindows();
            int position = 0;
            for (ConsultationProductDetailsDto.AvailabilityWindowDto slot : slots) {
                try {
                    ConsultationAvailabilityWindow window = new ConsultationAvailabilityWindow();
                    window.setDay(day);
                    window.setStartTime(LocalTime.parse(slot.getStartTime(), DateTimeFormatter.ofPattern("HH:mm")));
                    window.setEndTime(LocalTime.parse(slot.getEndTime(), DateTimeFormatter.ofPattern("HH:mm")));
                    window.setPosition(position++);
                    day.getWindows().add(window);
                } catch (NullPointerException | DateTimeParseException ex) {
                    throw new IllegalArgumentException("Weekly availability times must use HH:mm syntax");
                }
            }
            replacement.add(day);
        }
        entity.getWeeklyAvailability().clear();
        entity.getWeeklyAvailability().addAll(replacement);
    }

    private List<ConsultationProductDetailsDto.AvailabilityDayDto> toAvailabilityDto(ConsultationProduct entity) {
        Map<ConsultationAvailabilityDay.Weekday, ConsultationAvailabilityDay> persisted =
                new EnumMap<>(ConsultationAvailabilityDay.Weekday.class);
        entity.getWeeklyAvailability().forEach(day -> persisted.put(day.getWeekday(), day));
        return java.util.Arrays.stream(ConsultationAvailabilityDay.Weekday.values()).map(weekday -> {
            ConsultationAvailabilityDay day = persisted.get(weekday);
            ConsultationProductDetailsDto.AvailabilityDayDto dto = new ConsultationProductDetailsDto.AvailabilityDayDto();
            dto.setDay(ConsultationProductDetailsDto.Weekday.valueOf(weekday.name()));
            dto.setEnabled(day != null && day.isEnabled());
            dto.setWindows(day == null ? List.of() : day.getWindows().stream().map(window -> {
                ConsultationProductDetailsDto.AvailabilityWindowDto slot = new ConsultationProductDetailsDto.AvailabilityWindowDto();
                slot.setStartTime(window.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                slot.setEndTime(window.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                return slot;
            }).toList());
            return dto;
        }).toList();
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
