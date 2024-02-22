package com.muralis.minhasfinancas.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MaxCodePointsValidator implements ConstraintValidator<MaxCodePoints, String> {
    private int maxValue;

    @Override
    public void initialize(MaxCodePoints constraintAnnotation) {
        this.maxValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.codePointCount(0, value.length()) <= maxValue;
    }
}