package com.github.mhdirkse.codegen.plugin.impl;

interface FieldServiceCallback {
    Status getStatusAccessModifierError(String modifier);
    Status getStatusTypeMismatch(Class<?> actual);
    Status getStatusFieldGetError();
    Status getStatusFieldSetError();
}
