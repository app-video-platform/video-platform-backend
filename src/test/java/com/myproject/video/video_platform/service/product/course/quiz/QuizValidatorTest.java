package com.myproject.video.video_platform.service.product.course.quiz;

import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizOptionDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizQuestionDto;
import com.myproject.video.video_platform.exception.product.QuizValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuizValidatorTest {

    private final QuizValidator validator = new QuizValidator();

    @Test
    void acceptsValidQuiz() {
        QuizDraftDto dto = sampleQuiz();
        assertDoesNotThrow(() -> validator.validateDraft(dto));
    }

    @Test
    void rejectsInvalidPassingScore() {
        QuizDraftDto dto = sampleQuiz();
        dto.setPassingScore(150);
        QuizValidationException ex = assertThrows(QuizValidationException.class, () -> validator.validateDraft(dto));
        assertEquals("Passing score must be between 0 and 100", ex.getErrors().get("passingScore"));
    }

    @Test
    void enforcesTrueFalseOptions() {
        QuizDraftDto dto = sampleQuiz();
        QuizQuestionDto question = dto.getQuestions().get(0);
        question.setType("true_false");
        question.setOptions(List.of(option("True", true), option("True", false)));

        QuizValidationException ex = assertThrows(QuizValidationException.class, () -> validator.validateDraft(dto));
        assertEquals("True/False questions must contain True and False options", ex.getErrors().get("questions[0].options"));
    }

    private QuizDraftDto sampleQuiz() {
        QuizDraftDto dto = new QuizDraftDto();
        dto.setTitle("Sample");
        dto.setPassingScore(70);
        QuizQuestionDto question = new QuizQuestionDto();
        question.setTitle("Question");
        question.setType("multiple_choice_single");
        question.setPoints(5);
        question.setOptions(List.of(option("A", true), option("B", false)));
        dto.setQuestions(List.of(question));
        return dto;
    }

    private QuizOptionDto option(String text, boolean correct) {
        QuizOptionDto option = new QuizOptionDto();
        option.setText(text);
        option.setIsCorrect(correct);
        return option;
    }
}
