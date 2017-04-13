package com.catfish.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;
import java.lang.annotation.*;

/**
 * Created by A on 2017/3/24.
 */
@Max(10000)
@Min(8000)
@Constraint(validatedBy = {})
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Price {
    String message() default "错误的价格";

    Class<?>[] groups() default {Default.class};

    Class<? extends Payload>[] payload() default {};
}
