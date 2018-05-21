package com.github.mhdirkse.codegen.runtime;

interface HandlerRunner<H> {
    boolean run(H handler, HandlerStackContext ctx);
}
