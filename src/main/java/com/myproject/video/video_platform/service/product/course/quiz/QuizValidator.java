package com.myproject.video.video_platform.service.product.course.quiz;

import com.myproject.video.video_platform.common.enums.products.course.QuizQuestionType;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizOptionDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizQuestionDto;
import com.myproject.video.video_platform.exception.product.QuizValidationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class QuizValidator {

    public void validateDraft(QuizDraftDto dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto.getPassingScore() != null && (dto.getPassingScore() < 0 || dto.getPassingScore() > 100)) {
                errors.put("passingScore", "Passing score must be between 0 and 100");
            }


        List<QuizQuestionDto> questions = dto.getQuestions();
        if (questions == null || questions.isEmpty()) {
            errors.put("questions", "At least one question is required");
        } else {
            for (int i = 0; i < questions.size(); i++) {
                QuizQuestionDto question = questions.get(i);
                String basePath = "questions[" + i + "]";
                validateQuestion(question, basePath, errors);
            }
        }

        if (!errors.isEmpty()) {
            throw new QuizValidationException("Quiz validation failed", errors);
        }
    }

    private void validateQuestion(QuizQuestionDto question, String basePath, Map<String, String> errors) {
        if (!StringUtils.hasText(question.getTitle())) {
            errors.put(basePath + ".title", "Question title is required");
        }

        QuizQuestionType type = null;
        try {
            type = QuizQuestionType.fromValue(question.getType());
        } catch (IllegalArgumentException e) {
            errors.put(basePath + ".type", "Unsupported question type");
        }

        if (question.getPoints() == null || question.getPoints() <= 0) {
            errors.put(basePath + ".points", "Points must be greater than 0");
        }

        List<QuizOptionDto> options = question.getOptions();
        if (options == null || options.size() < 2) {
            errors.put(basePath + ".options", "Each question must have at least two options");
            return;
        }

        Set<String> normalizedTrueFalse = new HashSet<>();
        int correctCount = 0;
        for (int j = 0; j < options.size(); j++) {
            QuizOptionDto option = options.get(j);
            String optionPath = basePath + ".options[" + j + "]";
            if (!StringUtils.hasText(option.getText())) {
                errors.put(optionPath + ".text", "Option text is required");
            }
            if (Boolean.TRUE.equals(option.getIsCorrect())) {
                correctCount++;
            }
            if (type == QuizQuestionType.TRUE_FALSE && option.getText() != null) {
                normalizedTrueFalse.add(option.getText().trim().toLowerCase(Locale.ROOT));
            }
        }

        if (type == null) {
            return;
        }

        switch (type) {
            case MULTIPLE_CHOICE_SINGLE -> {
                if (correctCount != 1) {
                    errors.put(basePath + ".options", "Single choice questions must have exactly one correct option");
                }
            }
            case MULTIPLE_CHOICE_MULTI -> {
                if (correctCount < 1) {
                    errors.put(basePath + ".options", "Multi choice questions must have at least one correct option");
                }
            }
            case TRUE_FALSE -> {
                if (options.size() != 2) {
                    errors.put(basePath + ".options", "True/False questions require exactly two options");
                }
                if (!normalizedTrueFalse.contains("true") || !normalizedTrueFalse.contains("false")) {
                    errors.put(basePath + ".options", "True/False questions must contain True and False options");
                }
                if (correctCount != 1) {
                    errors.put(basePath + ".options", "True/False questions must have exactly one correct option");
                }
            }
        }
    }
}
