package com.myproject.video.video_platform.service.product.course.quiz;

import com.myproject.video.video_platform.common.enums.products.course.QuizQuestionType;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionAnswerDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionRequest;
import com.myproject.video.video_platform.entity.products.course.quiz.Quiz;
import com.myproject.video.video_platform.entity.products.course.quiz.QuizOption;
import com.myproject.video.video_platform.entity.products.course.quiz.QuizQuestion;
import com.myproject.video.video_platform.exception.product.QuizValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizSubmissionEvaluatorTest {

    private final QuizSubmissionEvaluator evaluator = new QuizSubmissionEvaluator();

    @Test
    void scoresSingleChoiceQuestion() {
        QuizOption correct = option(true);
        Quiz quiz = quizWithQuestion(QuizQuestionType.MULTIPLE_CHOICE_SINGLE, 5, List.of(correct, option(false)));

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        QuizSubmissionAnswerDto answer = new QuizSubmissionAnswerDto();
        answer.setQuestionId(quiz.getQuestions().get(0).getId().toString());
        answer.setSelectedOptionIds(List.of(correct.getId().toString()));
        request.setAnswers(List.of(answer));

        QuizSubmissionEvaluator.QuizSubmissionEvaluation result = evaluator.evaluate(quiz, request);
        assertEquals(5, result.totalPointsAchieved());
        assertEquals(5, result.totalPointsPossible());
        assertEquals(0, result.percentage().compareTo(BigDecimal.valueOf(100.0)));
        assertTrue(result.passed());
    }

    @Test
    void multiChoiceRequiresExactMatch() {
        QuizOption correctA = option(true);
        QuizOption correctB = option(true);
        QuizOption wrong = option(false);
        Quiz quiz = quizWithQuestion(QuizQuestionType.MULTIPLE_CHOICE_MULTI, 4, List.of(correctA, correctB, wrong));

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        QuizSubmissionAnswerDto answer = new QuizSubmissionAnswerDto();
        answer.setQuestionId(quiz.getQuestions().get(0).getId().toString());
        answer.setSelectedOptionIds(List.of(correctA.getId().toString()));
        request.setAnswers(List.of(answer));

        QuizSubmissionEvaluator.QuizSubmissionEvaluation result = evaluator.evaluate(quiz, request);
        assertEquals(0, result.totalPointsAchieved());
        assertEquals(4, result.totalPointsPossible());
        assertEquals(0, result.percentage().compareTo(BigDecimal.ZERO));
    }

    @Test
    void enforcesPassingThresholdWhenConfigured() {
        Quiz quiz = quizWithQuestion(QuizQuestionType.MULTIPLE_CHOICE_SINGLE, 10, List.of(option(true), option(false)));
        quiz.setPassingScore(80);

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setAnswers(List.of()); // unanswered -> zero points

        QuizSubmissionEvaluator.QuizSubmissionEvaluation result = evaluator.evaluate(quiz, request);
        assertEquals(0, result.totalPointsAchieved());
        assertEquals(0, result.percentage().compareTo(BigDecimal.ZERO));
        assertFalse(result.passed());
    }

    @Test
    void trueFalseValidatesSelections() {
        Quiz quiz = quizWithQuestion(QuizQuestionType.TRUE_FALSE, 2, List.of(option("true", true), option("false", false)));

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        QuizSubmissionAnswerDto answer = new QuizSubmissionAnswerDto();
        answer.setQuestionId(quiz.getQuestions().get(0).getId().toString());
        // selecting both options should be rejected
        answer.setSelectedOptionIds(quiz.getQuestions().get(0).getOptions().stream().map(opt -> opt.getId().toString()).toList());
        request.setAnswers(List.of(answer));

        assertThrows(QuizValidationException.class, () -> evaluator.evaluate(quiz, request));
    }

    private Quiz quizWithQuestion(QuizQuestionType type, int points, List<QuizOption> options) {
        Quiz quiz = new Quiz();
        quiz.setId(UUID.randomUUID());

        QuizQuestion question = new QuizQuestion();
        question.setId(UUID.randomUUID());
        question.setType(type);
        question.setPoints(points);
        question.setTitle("Question");
        question.setQuiz(quiz);
        for (QuizOption option : options) {
            question.addOption(option);
        }
        quiz.addQuestion(question);
        return quiz;
    }

    private QuizOption option(boolean correct) {
        return option("Option", correct);
    }

    private QuizOption option(String text, boolean correct) {
        QuizOption option = new QuizOption();
        option.setId(UUID.randomUUID());
        option.setText(text);
        option.setCorrect(correct);
        option.setPosition(1);
        return option;
    }
}
