package com.catfish.element;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * Created by A on 2017/4/10.
 */
public class ValidaElement<A extends Annotation,T> implements Serializable {
    private String propertyPath;
    private T values;
    private Class<A> annotation;
    private Class<?>[] groups;


    public ValidaElement(String name, T values, Class<A> annotation, Class[] groups) {
        this.propertyPath = name;
        this.values = values;
        this.annotation = annotation;
        this.groups = groups;
    }

    public String getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(String name) {
        this.propertyPath = name;
    }

    public Class[] getGroups() {
        return groups;
    }

    public void setGroups(Class[] groups) {
        this.groups = groups;
    }

    public T getValues() {
        return values;
    }

    public void setValues(T values) {
        this.values = values;
    }

    public Class<? extends Annotation> getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Class<A> annotation) {
        this.annotation = annotation;
    }
}
