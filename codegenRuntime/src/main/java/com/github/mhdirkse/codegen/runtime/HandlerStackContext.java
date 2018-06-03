package com.github.mhdirkse.codegen.runtime;

public interface HandlerStackContext<H> {
    boolean isFirst();
    boolean isLast();
    H getPreviousHandler();
    H getNextHandler();
    void addFirst(H handler);
    void removeFirst();
    void removeAllPreceeding();
}
