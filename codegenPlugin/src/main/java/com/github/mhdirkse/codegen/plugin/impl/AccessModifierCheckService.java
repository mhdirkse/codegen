package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class AccessModifierCheckService {
    private final ServiceFactory sf;
    private final AccessModifierErrorCallback callback;

    AccessModifierCheckService(final ServiceFactory sf, final AccessModifierErrorCallback callback) {
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
