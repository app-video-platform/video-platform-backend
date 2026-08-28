package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.entity.products.consultation.*;
import com.myproject.video.video_platform.entity.products.membership.MembershipProduct;
import com.myproject.video.video_platform.exception.product.ProductPublicationValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductPublicationValidatorTest {
    private final ProductPublicationValidator validator = new ProductPublicationValidator();

    @Test
    void draftMayBeIncompleteButPublishedConsultationMustBeReady() {
        ConsultationProduct product = new ConsultationProduct();
        product.setStatus(ProductStatus.DRAFT);
        assertDoesNotThrow(() -> validator.validate(product));
        product.setStatus(ProductStatus.PUBLISHED);
        ProductPublicationValidationException error = assertThrows(ProductPublicationValidationException.class,
                () -> validator.validate(product));
        assertTrue(error.getErrors().containsKey("details.weeklyAvailability"));
    }

    @Test
    void validConsultationAvailabilityPublishesAndOverlapDoesNot() {
        ConsultationProduct product = new ConsultationProduct();
        product.setStatus(ProductStatus.PUBLISHED); product.setName("Office hours"); product.setPrice(BigDecimal.TEN);
        product.setDurationMinutes(30); product.setMeetingMethod(ConsultationProduct.MeetingMethod.ZOOM);
        ConsultationAvailabilityDay day = new ConsultationAvailabilityDay(); day.setConsultation(product); day.setEnabled(true);
        day.setWeekday(ConsultationAvailabilityDay.Weekday.MONDAY);
        day.getWindows().add(window(day, "09:00", "10:00", 0)); product.getWeeklyAvailability().add(day);
        assertDoesNotThrow(() -> validator.validate(product));
        day.getWindows().add(window(day, "09:30", "11:00", 1));
        assertThrows(ProductPublicationValidationException.class, () -> validator.validate(product));
    }

    @Test
    void membershipPublicationReturnsReadinessIssue() {
        MembershipProduct product = new MembershipProduct(); product.setStatus(ProductStatus.PUBLISHED); product.setName("Club");
        ProductPublicationValidationException error = assertThrows(ProductPublicationValidationException.class,
                () -> validator.validate(product));
        assertEquals("Membership publication is not supported yet", error.getErrors().get("status"));
    }

    private static ConsultationAvailabilityWindow window(ConsultationAvailabilityDay day, String start, String end, int position) {
        ConsultationAvailabilityWindow window = new ConsultationAvailabilityWindow(); window.setDay(day);
        window.setStartTime(LocalTime.parse(start)); window.setEndTime(LocalTime.parse(end)); window.setPosition(position); return window;
    }
}
