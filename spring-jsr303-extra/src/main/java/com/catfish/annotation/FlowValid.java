package com.catfish.annotation;

import java.lang.annotation.*;

/**
 * Created by apple on 17/4/9.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FlowValid {
    Class<? extends Annotation>[] value();
}
