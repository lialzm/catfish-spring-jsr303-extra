package com.catfish.validator;


import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import java.util.Set;

/**
 * Created by A on 2017/4/7.
 */
public class EmployeeValidator implements Validator {


    public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>[] groups) {
        return null;
    }

    public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>[] groups) {
        return null;
    }

    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>[] groups) {
        return null;
    }

    public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
        return null;
    }

    public <T> T unwrap(Class<T> type) {
        return null;
    }

    public ExecutableValidator forExecutables() {
        return null;
    }
}
