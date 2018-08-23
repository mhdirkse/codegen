package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;

interface AccessModifierErrorCallback {
    Status getStatusAccessModifierError(String modifier);
    Status getStatusAccessModifierError(Field field, String modifier);
}
