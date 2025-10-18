package com.myproject.video.video_platform.repository.products.consultation;

import com.myproject.video.video_platform.entity.products.consultation.ConnectedCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConnectedCalendarRepository extends JpaRepository<ConnectedCalendar, UUID> {
    List<ConnectedCalendar> findAllByTeacherId(UUID teacherId);
}
