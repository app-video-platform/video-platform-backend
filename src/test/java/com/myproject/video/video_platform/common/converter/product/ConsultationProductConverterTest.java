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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConsultationProductConverterTest {

    private final ConnectedCalendarRepository calendarRepository = Mockito.mock(ConnectedCalendarRepository.class);
    private final ConsultationProductConverter converter = new ConsultationProductConverter(calendarRepository);

    @Test
    void fromDto_detailsNull_setsOnlyBaseFields() {
        User owner = new User();
        owner.setUserId(UUID.randomUUID());

        ConsultationProductRequestDto dto = new ConsultationProductRequestDto();
        dto.setName("Consult");
        dto.setDescription("Desc");
        dto.setStatus(null);
        dto.setPrice(null);
        dto.setDetails(null);

        ConsultationProduct entity = converter.fromDto(dto, owner);

        assertEquals("Consult", entity.getName());
        assertEquals("Desc", entity.getDescription());
        assertEquals(ProductType.CONSULTATION, entity.getType());
        assertEquals(ProductStatus.DRAFT, entity.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(entity.getPrice()));
        assertEquals(owner.getUserId(), entity.getUser().getUserId());

        assertNull(entity.getDurationMinutes());
        assertNull(entity.getMeetingMethod());
        assertNull(entity.getCancellationPolicy());
    }

    @Test
    void fromDto_withDetails_mapsConsultationFields() {
        User owner = new User();
        owner.setUserId(UUID.randomUUID());

        ConsultationProductDetailsDto details = new ConsultationProductDetailsDto();
        details.setDurationMinutes(60);
        details.setMeetingMethod(ConsultationProductDetailsDto.MeetingMethod.ZOOM);
        details.setBufferBeforeMinutes(5);
        details.setBufferAfterMinutes(10);
        details.setCancellationPolicy("No refunds");

        ConsultationProductRequestDto dto = new ConsultationProductRequestDto();
        dto.setName("Consult");
        dto.setStatus("PUBLISHED");
        dto.setPrice("19.99");
        dto.setDetails(details);

        ConsultationProduct entity = converter.fromDto(dto, owner);
        assertEquals(60, entity.getDurationMinutes());
        assertEquals(ConsultationProduct.MeetingMethod.ZOOM, entity.getMeetingMethod());
        assertEquals(5, entity.getBufferBeforeMinutes());
        assertEquals(10, entity.getBufferAfterMinutes());
        assertEquals("No refunds", entity.getCancellationPolicy());
        assertEquals(ProductStatus.PUBLISHED, entity.getStatus());
        assertEquals(0, BigDecimal.valueOf(19.99).compareTo(entity.getPrice()));
    }

    @Test
    void updateEntityFromDto_detailsNull_doesNotChangeConsultationFields() {
        ConsultationProduct entity = new ConsultationProduct();
        entity.setName("Old");
        entity.setStatus(ProductStatus.PUBLISHED);
        entity.setPrice(BigDecimal.TEN);
        entity.setDurationMinutes(45);
        entity.setMeetingMethod(ConsultationProduct.MeetingMethod.PHONE);
        entity.setCancellationPolicy("Old policy");

        ConsultationProductRequestDto dto = new ConsultationProductRequestDto();
        dto.setName("New");
        dto.setDetails(null);

        converter.updateEntityFromDto(dto, entity);

        assertEquals("New", entity.getName());
        assertEquals(45, entity.getDurationMinutes());
        assertEquals(ConsultationProduct.MeetingMethod.PHONE, entity.getMeetingMethod());
        assertEquals("Old policy", entity.getCancellationPolicy());
    }

    @Test
    void updateEntityFromDto_partialDetails_updatesOnlyProvided() {
        ConsultationProduct entity = new ConsultationProduct();
        entity.setDurationMinutes(45);
        entity.setCustomLocation("A");
        entity.setCancellationPolicy("Old");

        ConsultationProductDetailsDto details = new ConsultationProductDetailsDto();
        details.setCustomLocation("B");

        ConsultationProductRequestDto dto = new ConsultationProductRequestDto();
        dto.setDetails(details);

        converter.updateEntityFromDto(dto, entity);

        assertEquals(45, entity.getDurationMinutes(), "Null durationMinutes must not overwrite existing");
        assertEquals("B", entity.getCustomLocation());
        assertEquals("Old", entity.getCancellationPolicy(), "Null cancellationPolicy must not overwrite existing");
    }

    @Test
    void toDto_includesConnectedCalendarsInsideDetails() {
        UUID teacherId = UUID.randomUUID();
        User owner = new User();
        owner.setUserId(teacherId);

        ConsultationProduct entity = new ConsultationProduct();
        entity.setId(UUID.randomUUID());
        entity.setType(ProductType.CONSULTATION);
        entity.setStatus(ProductStatus.DRAFT);
        entity.setName("Consult");
        entity.setUser(owner);
        entity.setPrice(BigDecimal.ZERO);

        ConnectedCalendar cal = ConnectedCalendar.builder()
                .id(UUID.randomUUID())
                .teacherId(teacherId)
                .provider(ConnectedCalendar.Provider.GOOGLE)
                .oauthTokenEnc("enc")
                .refreshTokenEnc("enc2")
                .expiresAt(ZonedDateTime.now())
                .build();
        Mockito.when(calendarRepository.findAllByTeacherId(teacherId)).thenReturn(List.of(cal));

        ConsultationProductResponseDto dto = converter.toDto(entity);
        assertEquals("CONSULTATION", dto.getType());
        assertNotNull(dto.getDetails());
        assertNotNull(dto.getDetails().getConnectedCalendars());
        assertEquals(1, dto.getDetails().getConnectedCalendars().size());
        assertEquals(cal.getId().toString(), dto.getDetails().getConnectedCalendars().get(0).getId());
        assertEquals("GOOGLE", dto.getDetails().getConnectedCalendars().get(0).getProvider());
    }
}

