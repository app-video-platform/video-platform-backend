package com.myproject.video.video_platform.service.product.course;

import com.myproject.video.video_platform.common.enums.products.course.LessonType;
import com.myproject.video.video_platform.dto.products.course.CourseLessonCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonUpdateRequestDto;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseLessonService {

    private final CourseSectionRepository sectionRepo;
    private final CourseLessonRepository lessonRepo;
    private final CurrentUserService currentUserService;

    /**
     * POST /api/sections/{sectionId}/lessons
     */
    @Transactional
    public CourseLessonResponseDto createLesson(CourseLessonCreateRequestDto dto) {

        UUID sectionId = UUID.fromString(dto.getSectionId());
        CourseSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!section.getCourse().getUser().getUserId().equals(currentUserId))
            throw new AccessDeniedException("You don’t own this product.");


        CourseLesson lesson = new CourseLesson();
        lesson.setTitle(dto.getTitle());
        LessonType lessonType = LessonType.valueOf(dto.getType().toUpperCase());
        lesson.setType(lessonType);
        applyLessonContent(lesson, lessonType, dto.getVideoUrl(), dto.getContent());
        lesson.setPosition(
                dto.getPosition() == null
                        ? section.getLessons().size() + 1
                        : dto.getPosition()
        );
        lesson.setSection(section);

        section.getLessons().add(lesson);
        lessonRepo.save(lesson); // cascade saves lesson

        CourseLessonResponseDto resp = new CourseLessonResponseDto();
        resp.setId(lesson.getId().toString());
        resp.setTitle(lesson.getTitle());
        resp.setType(lesson.getType().name());
        resp.setVideoUrl(lesson.getVideoUrl());
        resp.setContent(lesson.getContent());
        resp.setPosition(lesson.getPosition());
        return resp;
    }

    /**
     * PUT /api/lessons/{lessonId}
     */
    @Transactional
    public void updateLesson(CourseLessonUpdateRequestDto dto) {
        UUID lessonId = UUID.fromString(dto.getId());
        CourseLesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + dto.getId()));

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!lesson.getSection().getCourse().getUser().getUserId().equals(currentUserId))
            throw new AccessDeniedException("You don’t own this product.");

        lesson.setTitle(dto.getTitle());
        LessonType newType = LessonType.valueOf(dto.getType().toUpperCase());
        lesson.setType(newType);
        applyLessonContent(lesson, newType, dto.getVideoUrl(), dto.getContent());
        if (dto.getPosition() != null) {
            lesson.setPosition(dto.getPosition());
        }
        lessonRepo.save(lesson);
    }

    public void deleteLesson(String userId, String lessonId) {
        Optional<CourseLesson> lessonOptional = lessonRepo.findById(UUID.fromString(lessonId));
        if (lessonOptional.isPresent()) {
            UUID currentUserId = currentUserService.getCurrentUserId();
            log.info("User id from context: {}", currentUserId);

            if (!lessonOptional.get().getSection().getCourse().getUser().getUserId().equals(currentUserId))
                throw new AccessDeniedException("You don’t own this product.");

            lessonRepo.delete(lessonOptional.get());
            log.info("Deleted succesfully a Course lesson: {}", lessonId);
        } else
            throw new ResourceNotFoundException("Course lesson not found for ID: " + lessonId);

}

    private void applyLessonContent(CourseLesson lesson, LessonType type, String videoUrl, String content) {
        switch (type) {
            case VIDEO -> {
                lesson.setVideoUrl(videoUrl);
                lesson.setContent(null);
            }
            case ARTICLE -> {
                lesson.setContent(content);
                lesson.setVideoUrl(null);
            }
            case QUIZ -> {
                lesson.setVideoUrl(null);
                lesson.setContent(null);
            }
        }
    }
}
