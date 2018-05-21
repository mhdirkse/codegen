package com.github.mhdirkse.codegen.runtime;

interface HandlerStackManipulator<H> {
    void addFirst(H handler);
    void removeFirst();
}