package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;

import org.apache.commons.lang.ArrayUtils;

import lombok.AccessLevel;
import lombok.Getter;

abstract class Status {
    @Getter
    private final StatusCode statusCode;

    Status(final StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    abstract LogPriority getLogPriority();
    abstract String[] getArguments();

    private static class FieldError extends Status {
        private final String[] arguments;

        FieldError(final StatusCode statusCode, final Field field, final String... otherArguments) {
            super(statusCode);
            arguments = (String[])
                    ArrayUtils.addAll(
                    new String[] {field.getName()}, otherArguments);
        }

        @Override
        LogPriority getLogPriority() {
            return LogPriority.ERROR;
        }

        @Override
        public String[] getArguments() {
            return arguments;
        }
    }

    static Status forFieldError(
            final StatusCode statusCode,
            final Field field,
            final String... otherArguments) {
        return new FieldError(statusCode, field, otherArguments);
    }

    @Getter(value = AccessLevel.PACKAGE, onMethod = @__({@Override}))
    private static class GeneralStatus extends Status {
        private final LogPriority logPriority;
        private final String[] arguments;

        GeneralStatus(
                final StatusCode statusCode,
                final LogPriority logPriority,
                final String... arguments) {
            super(statusCode);
            this.logPriority = logPriority;
            this.arguments = arguments;
        }
    }

    static Status general(
            final StatusCode statusCode,
            final LogPriority logPriority,
            final String... arguments) {
        return new GeneralStatus(statusCode, logPriority, arguments);
    }
}
