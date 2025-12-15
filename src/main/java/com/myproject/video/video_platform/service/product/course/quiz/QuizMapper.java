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
        dto.setPassingScore(quiz.getPassingScore());

        List<QuizQuestionDto> questions = quiz.getQuestions().stream()
                .sorted(Comparator.comparing(q -> q.getPosition() == null ? Integer.MAX_VALUE : q.getPosition()))
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
        questionDto.setPosition(question.getPosition());

        List<QuizOptionDto> optionDtos = question.getOptions().stream()
                .sorted(Comparator.comparing(o -> o.getPosition() == null ? Integer.MAX_VALUE : o.getPosition()))
                .map(option -> mapOption(option, includeCorrectness))
                .collect(Collectors.toList());
        questionDto.setOptions(optionDtos);
        return questionDto;
    }

    private QuizOptionDto mapOption(QuizOption option, boolean includeCorrectness) {
        QuizOptionDto optionDto = new QuizOptionDto();
        optionDto.setId(option.getId().toString());
        optionDto.setText(option.getText());
        optionDto.setPosition(option.getPosition());
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
            Integer position = questionDto.getPosition() != null
                    ? questionDto.getPosition()
                    : questionOrder.incrementAndGet();
            question.setPosition(position);

            question.getOptions().clear();
            AtomicInteger optionOrder = new AtomicInteger(0);
            for (QuizOptionDto optionDto : questionDto.getOptions()) {
                QuizOption option = new QuizOption();
                option.setId(resolveUuid(optionDto.getId(), null));
                option.setText(optionDto.getText());
                option.setCorrect(Boolean.TRUE.equals(optionDto.getIsCorrect()));
                Integer optionPosition = optionDto.getPosition() != null
                        ? optionDto.getPosition()
                        : optionOrder.incrementAndGet();
                option.setPosition(optionPosition);
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
