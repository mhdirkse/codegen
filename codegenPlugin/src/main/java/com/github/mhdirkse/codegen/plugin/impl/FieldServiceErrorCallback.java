package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;

interface FieldServiceErrorCallback {
    Status getStatusAccessModifierError(String modifier);
    Status getStatusAccessModifierError(Field field, String modifier);
    Status getStatusTypeMismatch(Class<?> actual);
    Status getStatusFieldGetError();
    Status getStatusFieldSetError();
}
