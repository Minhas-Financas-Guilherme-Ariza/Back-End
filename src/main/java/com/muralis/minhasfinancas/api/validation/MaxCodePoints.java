package com.muralis.minhasfinancas.api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = {MaxCodePointsValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxCodePoints {

	String message() default "O número de code points não pode exceder o valor especificado";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int value();
}
