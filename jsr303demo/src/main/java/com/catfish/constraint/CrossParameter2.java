package com.catfish.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by A on 2017/4/11.
 */
@Constraint(validatedBy = CrossParameterValidator2.class)
@Target({ METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface CrossParameter2 {

    String message() default "密码错误";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };

}
