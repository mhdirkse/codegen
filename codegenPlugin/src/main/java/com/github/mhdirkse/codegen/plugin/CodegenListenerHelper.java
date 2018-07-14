package com.github.mhdirkse.codegen.plugin;

import java.util.List;

import org.antlr.v4.runtime.Token;

import com.github.mhdirkse.codegen.plugin.model.ClassModel;
import com.github.mhdirkse.codegen.plugin.model.MethodModel;

interface CodegenListenerHelper {
    List<MethodModel> getMethods(String fullClassName) throws ClassNotFoundException;
    void logInfo(String message);
    void logError(final int line, final int column, final String msg);
    String checkCommonReturnType(final ClassModel source, final Token startToken);
    boolean getHasErrors();
}
