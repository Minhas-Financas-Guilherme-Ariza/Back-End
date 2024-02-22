package com.muralis.minhasfinancas.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StatusPendenteValidator implements ConstraintValidator<StatusPendente, String>{

	 private static final String STATUS_PATTERN = "PENDENTE|CANCELADO|EFETIVADO";

	    @Override
	    public boolean isValid(String value, ConstraintValidatorContext context) {
	        if (!value.matches(STATUS_PATTERN)) {
	            value = "PENDENTE";
	        }

	        return true;
	    }
	}