package com.myproject.video.video_platform.service.product.course.quiz;

import com.myproject.video.video_platform.common.enums.products.course.QuizQuestionType;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizOptionDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizQuestionDto;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.quiz.Quiz;
import com.myproject.video.video_platform.entity.products.course.quiz.QuizOption;
import com.myproject.video.video_platform.entity.products.course.quiz.QuizQuestion;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class QuizMapper {

    public QuizDraftDto toAuthoringDto(Quiz quiz) {
        return toDto(quiz, true);
    }

    public QuizDraftDto toPlayerDto(Quiz quiz) {
        return toDto(quiz, false);
    }

    private QuizDraftDto toDto(Quiz quiz, boolean includeCorrectness) {
        QuizDraftDto dto = new QuizDraftDto();
        dto.setId(quiz.getId().toString());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setPassingScore(quiz.getPassingScore());

        List<QuizQuestionDto> questions = quiz.getQuestions().stream()
                .sorted(Comparator.comparing(q -> q.getSortOrder() == null ? Integer.MAX_VALUE : q.getSortOrder()))
                .map(question -> mapQuestion(question, includeCorrectness))
                .collect(Collectors.toList());
        dto.setQuestions(questions);
        return dto;
    }

    private QuizQuestionDto mapQuestion(QuizQuestion question, boolean includeCorrectness) {
        QuizQuestionDto questionDto = new QuizQuestionDto();
        questionDto.setId(question.getId().toString());
        questionDto.setTitle(question.getTitle());
        questionDto.setType(question.getType().getValue());
        questionDto.setPoints(question.getPoints());
        questionDto.setExplanation(question.getExplanation());
        questionDto.setSortOrder(question.getSortOrder());

        List<QuizOptionDto> optionDtos = question.getOptions().stream()
                .sorted(Comparator.comparing(o -> o.getSortOrder() == null ? Integer.MAX_VALUE : o.getSortOrder()))
                .map(option -> mapOption(option, includeCorrectness))
                .collect(Collectors.toList());
        questionDto.setOptions(optionDtos);
        return questionDto;
    }

    private QuizOptionDto mapOption(QuizOption option, boolean includeCorrectness) {
        QuizOptionDto optionDto = new QuizOptionDto();
        optionDto.setId(option.getId().toString());
        optionDto.setText(option.getText());
        optionDto.setSortOrder(option.getSortOrder());
        if (includeCorrectness) {
            optionDto.setIsCorrect(option.isCorrect());
        }
        return optionDto;
    }

    public Quiz applyDraft(Quiz quiz, QuizDraftDto dto, CourseLesson lesson) {
        if (quiz.getId() == null) {
            quiz.setId(resolveUuid(dto.getId(), null));
        } else if (dto.getId() != null && !quiz.getId().toString().equals(dto.getId())) {
            quiz.setId(quiz.getId());
        }
        quiz.setLesson(lesson);
        lesson.setQuiz(quiz);
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setPassingScore(dto.getPassingScore());

        quiz.getQuestions().clear();
        AtomicInteger questionOrder = new AtomicInteger(0);
        for (QuizQuestionDto questionDto : dto.getQuestions()) {
            QuizQuestion question = new QuizQuestion();
            question.setId(resolveUuid(questionDto.getId(), null));
            question.setTitle(questionDto.getTitle());
            question.setType(QuizQuestionType.fromValue(questionDto.getType()));
            question.setPoints(questionDto.getPoints());
            question.setExplanation(questionDto.getExplanation());
            Integer sortOrder = questionDto.getSortOrder() != null
                    ? questionDto.getSortOrder()
                    : questionOrder.incrementAndGet();
            question.setSortOrder(sortOrder);

            question.getOptions().clear();
            AtomicInteger optionOrder = new AtomicInteger(0);
            for (QuizOptionDto optionDto : questionDto.getOptions()) {
                QuizOption option = new QuizOption();
                option.setId(resolveUuid(optionDto.getId(), null));
                option.setText(optionDto.getText());
                option.setCorrect(Boolean.TRUE.equals(optionDto.getIsCorrect()));
                Integer optionSortOrder = optionDto.getSortOrder() != null
                        ? optionDto.getSortOrder()
                        : optionOrder.incrementAndGet();
                option.setSortOrder(optionSortOrder);
                question.addOption(option);
            }

            quiz.addQuestion(question);
        }
        return quiz;
    }

    private UUID resolveUuid(String incomingId, UUID fallback) {
        if (incomingId != null) {
            try {
                return UUID.fromString(incomingId);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return fallback != null ? fallback : UUID.randomUUID();
    }
}
