package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
}
