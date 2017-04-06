package com.catfish.validator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by apple on 17/4/6.
 */
public class FlowValidator {

    public static  <T> void doValid(T t) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(t);
        if (violations.size() > 0) {
            StringBuffer buf = new StringBuffer();
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            for (ConstraintViolation<T> violation : violations) {
                buf.append("-" + bundle.getString(violation.getPropertyPath().toString()));
                buf.append(violation.getMessage() + "<BR>\n");
            }
        }
        return;
    }

}
