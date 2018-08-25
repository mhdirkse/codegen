package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;

class FieldService {
    interface Callback {
        Status getStatusAccessModifierError(String modifier);
        Status getStatusTypeMismatch(Class<?> actual);
        Status getStatusFieldValueIsNull();
        Status getStatusFieldGetError();
        Status getStatusFieldSetError();
    }

    private final ServiceFactory sf;

    FieldService(final ServiceFactory sf) {
        this.sf = sf;
    }

    boolean checkNotFinal(final Field field, final Callback callback) {
        boolean isFinal = Modifier.isFinal(field.getModifiers());
        if(isFinal) {
            Status status = callback.getStatusAccessModifierError("final");
            sf.reporter().report(status);
        }
        return !isFinal;
    }

    boolean checkNotStatic(final Field field, final Callback callback) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if(isStatic) {
            Status status = callback.getStatusAccessModifierError("static");
            sf.reporter().report(status);
        }
        return !isStatic;
    }

    <T> boolean checkType(final Field field, final Class<T> expectedType, final Callback callback) {
        Class<?> actualType = field.getType();
        if(!actualType.equals(expectedType)) {
            Status status = callback.getStatusTypeMismatch(actualType);
            sf.reporter().report(status);
            return false;
        } else {
            return true;
        }
    }

    Optional<Object> getField(final Field field, final Callback callback) {
        try {
            Object rawValue = field.get(sf.getProgram());
            if(Objects.isNull(rawValue)) {
                Status status = callback.getStatusFieldValueIsNull();
                sf.reporter().report(status);
            }
            return Optional.ofNullable(rawValue);
        }
        catch(Exception e) {
            Status status = callback.getStatusFieldGetError();
            sf.reporter().report(status, e);
            return Optional.empty();
        }
    }

    void setField(final Field field, final Object value, final Callback callback) {
        try {
            field.set(sf.getProgram(), value);
        }
        catch(Exception e) {
            Status status = callback.getStatusFieldSetError();
            sf.reporter().report(status, e);
        }
    }
}
