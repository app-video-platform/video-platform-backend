package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.products.consultation.ConsultationAvailabilityDay;
import com.myproject.video.video_platform.entity.products.consultation.ConsultationAvailabilityWindow;
import com.myproject.video.video_platform.entity.products.consultation.ConsultationProduct;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import com.myproject.video.video_platform.entity.products.membership.MembershipProduct;
import com.myproject.video.video_platform.exception.product.ProductPublicationValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductPublicationValidator {
    public void validate(Product product) {
        if (product.getStatus() != ProductStatus.PUBLISHED) return;
        Map<String, String> issues = new LinkedHashMap<>();
        if (product.getName() == null || product.getName().isBlank()) {
            issues.put("name", "Name is required");
        }
        if (product instanceof MembershipProduct) {
            issues.put("status", "Membership publication is not supported yet");
        } else if (product instanceof CourseProduct course) {
            requireFreeOrPositivePrice(product, issues);
            if (course.getSections() == null || course.getSections().isEmpty()) {
                issues.put("details.sections", "At least one section is required");
            } else if (course.getSections().stream().noneMatch(s -> s.getLessons() != null && !s.getLessons().isEmpty())) {
                issues.put("details.lessons", "At least one lesson is required");
            }
        } else if (product instanceof DownloadProduct download) {
            requireFreeOrPositivePrice(product, issues);
            boolean hasFile = download.getSectionDownloadProducts() != null
                    && download.getSectionDownloadProducts().stream()
                    .flatMap(section -> section.getFiles().stream())
                    .anyMatch(file -> file.getPath() != null && !file.getPath().isBlank());
            if (!hasFile) issues.put("details.files", "At least one confirmed file is required");
        } else if (product instanceof ConsultationProduct consultation) {
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                issues.put("price", "Consultations require a positive price");
            }
            if (consultation.getDurationMinutes() == null || consultation.getDurationMinutes() <= 0) {
                issues.put("details.durationMinutes", "Duration must be positive");
            }
            if (consultation.getMeetingMethod() == null) {
                issues.put("details.meetingMethod", "Meeting method is required");
            } else if (consultation.getMeetingMethod() == ConsultationProduct.MeetingMethod.OTHER
                    && (consultation.getCustomLocation() == null || consultation.getCustomLocation().isBlank())) {
                issues.put("details.customLocation", "A custom location is required for OTHER");
            }
            validateAvailability(consultation, issues);
        }
        if (!issues.isEmpty()) throw new ProductPublicationValidationException(issues);
    }

    private static void requireFreeOrPositivePrice(Product product, Map<String, String> issues) {
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            issues.put("price", "Price must be free or positive");
        }
    }

    private static void validateAvailability(ConsultationProduct product, Map<String, String> issues) {
        boolean validRangeFound = false;
        for (ConsultationAvailabilityDay day : product.getWeeklyAvailability()) {
            if (!day.isEnabled()) continue;
            List<ConsultationAvailabilityWindow> windows = day.getWindows();
            for (int index = 0; index < windows.size(); index++) {
                ConsultationAvailabilityWindow current = windows.get(index);
                if (!current.getStartTime().isBefore(current.getEndTime())) {
                    issues.put("details.weeklyAvailability", "Availability start time must be before end time");
                    continue;
                }
                validRangeFound = true;
                for (int other = index + 1; other < windows.size(); other++) {
                    ConsultationAvailabilityWindow candidate = windows.get(other);
                    if (current.getStartTime().isBefore(candidate.getEndTime())
                            && candidate.getStartTime().isBefore(current.getEndTime())) {
                        issues.put("details.weeklyAvailability", "Availability windows cannot overlap");
                    }
                }
            }
        }
        if (!validRangeFound) {
            issues.put("details.weeklyAvailability", "At least one enabled valid availability range is required");
        }
    }
}
