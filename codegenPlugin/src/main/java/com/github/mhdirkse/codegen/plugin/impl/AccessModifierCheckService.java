package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class AccessModifierCheckService {
    interface Callback {
        Status getStatusAccessModifierError(String modifier);
    }

    private final ServiceFactory sf;
    private final Callback callback;

    AccessModifierCheckService(final ServiceFactory sf, final Callback callback) {
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
}
