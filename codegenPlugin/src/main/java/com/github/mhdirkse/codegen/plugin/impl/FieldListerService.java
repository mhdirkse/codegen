package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.reflections.ReflectionUtils;

class FieldListerService {
    private final ServiceFactory sf;

    FieldListerService(final ServiceFactory sf) {
        this.sf = sf;
    }

    @SuppressWarnings("unchecked")
    <T extends Annotation> Set<Field> getFields(
            final Class<T> annotationClass,
            final FieldServiceErrorCallback callback) {
        Set<Field> allFields = ReflectionUtils.getAllFields(
                sf.getProgram().getClass(),
                ReflectionUtils.withAnnotation(annotationClass));
        Set<Field> onlyPublicFields = ReflectionUtils.getAllFields(
                sf.getProgram().getClass(),
                ReflectionUtils.withAnnotation(annotationClass),
                ReflectionUtils.withModifier(Modifier.PUBLIC));
        Set<Field> nonPublicFields = new HashSet<>(allFields);
        nonPublicFields.removeAll(onlyPublicFields);
        nonPublicFields.forEach(
                f -> sf.reporter().report(callback.getStatusAccessModifierError(f, "public")));
        return onlyPublicFields;
    }
}
