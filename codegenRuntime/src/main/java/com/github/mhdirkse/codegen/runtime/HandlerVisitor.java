package com.github.mhdirkse.codegen.runtime;

interface HandlerVisitor<H> {
    boolean onHandler(H handler, H prevH, H nextH);
    void afterStackVisited();
}