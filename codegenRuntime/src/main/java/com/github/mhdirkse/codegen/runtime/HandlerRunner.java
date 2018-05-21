package com.github.mhdirkse.codegen.runtime;

public interface HandlerRunner<H> {
    boolean run(H handler, HandlerStackContext<H> ctx);
}
