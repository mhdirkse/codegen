package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.lang.ArrayUtils;

import lombok.Getter;

@Getter
class Status {
    private final StatusCode statusCode;
    private final LogPriority logPriority;
    private final String[] arguments;

    private Status(final StatusCode statusCode, final LogPriority logPriority, final String... arguments) {
        this.statusCode = statusCode;
        this.logPriority = logPriority;
        this.arguments = arguments;
    }

    static Status forFieldError(
            final StatusCode statusCode,
            final Class<? extends Annotation> annotationClass,
            final Field field,
            final String... otherArguments) {
        String[] arguments = (String[])
            ArrayUtils.addAll(
                new String[] {
                    annotationClass.getSimpleName(),
                    field.getName()},
                otherArguments);
        return new Status(statusCode, LogPriority.ERROR, arguments);
    }

    static Status general(
            final StatusCode statusCode,
            final LogPriority logPriority,
            final String... arguments) {
        return new Status(statusCode, logPriority, arguments);
    }
}
