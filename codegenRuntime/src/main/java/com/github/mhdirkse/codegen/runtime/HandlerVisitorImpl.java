package com.github.mhdirkse.codegen.runtime;

public final class HandlerVisitorImpl<H>
implements HandlerVisitor<H>, HandlerStackContext<H> {
    private final HandlerRunner<H> runner;

    private H prevH;
    private H nextH;

    HandlerVisitorImpl(HandlerRunner<H> runner) {
        this.runner = runner;
        prevH = null;
        nextH = null;
    }

    @Override
    public boolean onHandler(H handler, H prevH, H nextH) {
        this.prevH = prevH;
        this.nextH = nextH;
        return runner.run(handler, this);
    }

    @Override
    public boolean isFirst() {
        return (prevH == null);
    }

    @Override
    public boolean isLast() {
        return (nextH == null);
    }

    @Override
    public H getPreviousHandler() {
        return prevH;
    }

    @Override
    public H getNextHandler() {
        return nextH;
    }
}
