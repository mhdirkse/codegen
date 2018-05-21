package com.github.mhdirkse.codegen.runtime;

public final class HandlerVisitorImpl<H> implements HandlerVisitor<H> {
    private final HandlerRunner<H> runner;

    HandlerVisitorImpl(HandlerRunner<H> runner) {
        this.runner = runner;
    }

    @Override
    public boolean onHandler(H handler) {
        // TODO: Implement.
        return true;
    }

    @Override
    public boolean onNoMoreHandlers() {
        return true;
    }
}
