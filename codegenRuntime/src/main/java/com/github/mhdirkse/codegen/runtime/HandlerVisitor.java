package com.github.mhdirkse.codegen.runtime;

public interface HandlerVisitor<H> {
    boolean onHandler(H handler, H prevH, H nextH);
}