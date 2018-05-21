package com.github.mhdirkse.codegen.runtime;

public interface HandlerStackContext<H> extends HandlerStackManipulator<H> {
    boolean isFirst();
    boolean isLast();
    H getPreviousHandler();
    H getNextHandler();
}
