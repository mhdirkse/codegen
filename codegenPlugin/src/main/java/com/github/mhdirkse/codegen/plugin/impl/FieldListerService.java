package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import org.reflections.ReflectionUtils;

class FieldListerService {
    private final ServiceFactory sf;

    FieldListerService(final ServiceFactory sf) {
        this.sf = sf;
    }

    @SuppressWarnings("unchecked")
    <T extends Annotation> Set<Field> getFields(final Class<T> annotationClass) {
        return ReflectionUtils.getAllFields(
                sf.getProgram().getClass(),
                ReflectionUtils.withAnnotation(annotationClass));
    }
}
