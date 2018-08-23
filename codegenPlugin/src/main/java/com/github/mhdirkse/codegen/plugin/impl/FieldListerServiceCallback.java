package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;

interface FieldListerServiceCallback {
    Status getStatusAccessModifierError(Field field, String modifier);
}
