package com.github.mhdirkse.codegen.plugin;

import java.util.List;

import com.github.mhdirkse.codegen.plugin.model.MethodModel;

interface CodegenListenerHelper {
    List<MethodModel> getMethods(String fullClassName) throws ClassNotFoundException;
    void logInfo(String message);
}
