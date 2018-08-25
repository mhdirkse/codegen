package com.github.mhdirkse.codegen.plugin.impl;

import java.util.function.Consumer;

public abstract class FileWriteService {
    public abstract void write(final FileContentsDefinition fcd, final Consumer<Exception> callback);
}
