package com.myproject.video.video_platform.common.enums.products.course;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QuizQuestionType {
    MULTIPLE_CHOICE_SINGLE("multiple_choice_single"),
    MULTIPLE_CHOICE_MULTI("multiple_choice_multi"),
    TRUE_FALSE("true_false");

    private final String value;

    QuizQuestionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static QuizQuestionType fromValue(String raw) {
        for (QuizQuestionType type : values()) {
            if (type.value.equalsIgnoreCase(raw)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported quiz question type: " + raw);
    }
}
