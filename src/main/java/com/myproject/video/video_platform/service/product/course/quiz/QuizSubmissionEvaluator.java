package com.myproject.video.video_platform.service.product.course.quiz;

import com.myproject.video.video_platform.common.enums.products.course.QuizQuestionType;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionAnswerDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionRequest;
import com.myproject.video.video_platform.entity.products.course.quiz.Quiz;
import com.myproject.video.video_platform.entity.products.course.quiz.QuizOption;
import com.myproject.video.video_platform.entity.products.course.quiz.QuizQuestion;
import com.myproject.video.video_platform.exception.product.QuizValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Contains the deterministic scoring rules for quiz submissions. When no passing score is configured on the quiz,
 * the evaluation treats every attempt as passed. This makes it explicit that no default threshold applies until one
 * is set on the quiz definition.
 */
@Component
public class QuizSubmissionEvaluator {

    public QuizSubmissionEvaluation evaluate(Quiz quiz, QuizSubmissionRequest request) {
        Map<UUID, QuizQuestion> questionMap = quiz.getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        Map<String, String> errors = new LinkedHashMap<>();
        Map<UUID, List<UUID>> normalizedAnswers = new LinkedHashMap<>();
        if (request.getAnswers() != null) {
            for (int i = 0; i < request.getAnswers().size(); i++) {
                QuizSubmissionAnswerDto answerDto = request.getAnswers().get(i);
                String basePath = "answers[" + i + "]";
                UUID questionId = parseUuid(answerDto.getQuestionId(), basePath + ".questionId", errors);
                if (questionId == null) {
                    continue;
                }
                QuizQuestion question = questionMap.get(questionId);
                if (question == null) {
                    errors.put(basePath + ".questionId", "Question does not belong to this quiz");
                    continue;
                }
                if (normalizedAnswers.containsKey(questionId)) {
                    errors.put(basePath + ".questionId", "Duplicate answer for question");
                    continue;
                }

                Set<UUID> selected = new LinkedHashSet<>();
                if (answerDto.getSelectedOptionIds() != null) {
                    for (int j = 0; j < answerDto.getSelectedOptionIds().size(); j++) {
                        String optionIdStr = answerDto.getSelectedOptionIds().get(j);
                        UUID optionId = parseUuid(optionIdStr, basePath + ".selectedOptionIds[" + j + "]", errors);
                        if (optionId == null) {
                            continue;
                        }
                        boolean belongs = question.getOptions().stream()
                                .anyMatch(option -> option.getId().equals(optionId));
                        if (!belongs) {
                            errors.put(basePath + ".selectedOptionIds[" + j + "]", "Option does not belong to the question");
                            continue;
                        }
                        selected.add(optionId);
                    }
                }

                if (!validateSelectionByType(question, selected, basePath, errors)) {
                    continue;
                }
                normalizedAnswers.put(questionId, new ArrayList<>(selected));
            }
        }

        if (!errors.isEmpty()) {
            throw new QuizValidationException("Invalid quiz submission", errors);
        }

        int totalPossible = questionMap.values().stream().mapToInt(QuizQuestion::getPoints).sum();
        int achieved = 0;
        List<QuestionResult> questionResults = new ArrayList<>();
        for (QuizQuestion question : quiz.getQuestions()) {
            List<UUID> selected = normalizedAnswers.getOrDefault(question.getId(), Collections.emptyList());
            boolean correct = evaluateQuestion(question, selected);
            int questionPoints = question.getPoints() == null ? 0 : question.getPoints();
            int awarded = correct ? questionPoints : 0;
            achieved += awarded;
            questionResults.add(new QuestionResult(question.getId(), correct, awarded, question.getExplanation()));
        }

        BigDecimal percentage = totalPossible == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(achieved)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalPossible), 1, RoundingMode.HALF_UP);

        boolean passed = quiz.getPassingScore() == null
                ? true
                : percentage.compareTo(BigDecimal.valueOf(quiz.getPassingScore())) >= 0;

        for (QuizQuestion question : quiz.getQuestions()) {
            normalizedAnswers.computeIfAbsent(question.getId(), q -> Collections.emptyList());
        }

        return new QuizSubmissionEvaluation(totalPossible, achieved, percentage, passed, questionResults, normalizedAnswers);
    }

    private boolean validateSelectionByType(QuizQuestion question, Set<UUID> selected, String path, Map<String, String> errors) {
        QuizQuestionType type = question.getType();
        if (type == QuizQuestionType.MULTIPLE_CHOICE_SINGLE || type == QuizQuestionType.TRUE_FALSE) {
            if (selected.size() > 1) {
                errors.put(path + ".selectedOptionIds", "Only one option may be selected for this question type");
                return false;
            }
        }
        return true;
    }

    private boolean evaluateQuestion(QuizQuestion question, List<UUID> selected) {
        QuizQuestionType type = question.getType();
        Set<UUID> correctOptions = question.getOptions().stream()
                .filter(QuizOption::isCorrect)
                .map(QuizOption::getId)
                .collect(Collectors.toSet());
        Set<UUID> selectedSet = new LinkedHashSet<>(selected);

        if (type == QuizQuestionType.MULTIPLE_CHOICE_MULTI) {
            return !selectedSet.isEmpty()
                    && selectedSet.equals(correctOptions);
        }
        return selectedSet.size() == 1 && correctOptions.containsAll(selectedSet);
    }

    private UUID parseUuid(String raw, String field, Map<String, String> errors) {
        if (raw == null) {
            errors.put(field, "Value is required");
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            errors.put(field, "Value must be a UUID");
            return null;
        }
    }

    public record QuestionResult(UUID questionId, boolean correct, int pointsAwarded, String explanation) {}

    public record QuizSubmissionEvaluation(
            int totalPointsPossible,
            int totalPointsAchieved,
            BigDecimal percentage,
            boolean passed,
            List<QuestionResult> questionResults,
            Map<UUID, List<UUID>> normalizedAnswers
    ) {}
}
