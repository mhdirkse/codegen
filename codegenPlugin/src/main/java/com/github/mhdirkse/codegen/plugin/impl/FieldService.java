package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

class FieldService {
    private final ServiceFactory sf;
    private final FieldServiceErrorCallback callback;

    FieldService(final ServiceFactory sf, final FieldServiceErrorCallback callback) {
        this.sf = sf;
        this.callback = callback;
    }

    void checkNotFinal(final Field field) {
        boolean isFinal = Modifier.isFinal(field.getModifiers());
        if(isFinal) {
            Status status = callback.getStatusAccessModifierError("final");
            sf.reporter().report(status);
        }
    }

    void checkNotStatic(final Field field) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if(isStatic) {
            Status status = callback.getStatusAccessModifierError("static");
            sf.reporter().report(status);
        }
    }

    <T> void checkType(final Field field, final Class<T> expectedType) {
        Class<?> actualType = field.getType();
        if(!actualType.equals(expectedType)) {
            Status status = callback.getStatusTypeMismatch(actualType);
            sf.reporter().report(status);
        }
    }

    Optional<Object> getField(final Field field, final Object subject) {
        try {
            return Optional.ofNullable(field.get(subject));
        }
        catch(Exception e) {
            Status status = callback.getStatusFieldGetError();
            sf.reporter().report(status, e);
            return Optional.empty();
        }
    }

    void setField(final Field field, final Object subject, final Object value) {
        try {
            field.set(subject, value);
        }
        catch(Exception e) {
            Status status = callback.getStatusFieldSetError();
            sf.reporter().report(status, e);
        }
    }
}
