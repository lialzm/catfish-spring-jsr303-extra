package com.catfish.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by A on 2017/4/11.
 */
public class CrossParameterValidator2 implements ConstraintValidator<CrossParameter2, Object[]> {

    @Override
    public void initialize(CrossParameter2 constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object[] value, ConstraintValidatorContext context) {
        if (value == null || value.length != 2) {
            throw new IllegalArgumentException("must have two args");
        }
        if (value[0] == null || value[1] == null) {
            return true;
        }
        if (value[0].equals(value[1])) {
            return true;
        }
        return false;
    }
}
