package com.muralis.minhasfinancas.api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;

@Documented
@Constraint(validatedBy = {StatusPendenteValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StatusPendente {
	
	String message() default "Esse STATUS é inválido. Alterado para PENDENTE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    
}
