package com.github.mhdirkse.codegen.runtime;

import java.util.ArrayList;
import java.util.List;

final class HandlerVisitorImpl<H>
implements HandlerVisitor<H>, HandlerStackContext<H> {
    private final HandlerRunner<H> runner;

    private H prevH;
    private H nextH;
    private int handlerSeq;

    private final HandlerStackManipulator<H> delegate;

    private List<Runnable> handlerStackChanges = new ArrayList<>();

    HandlerVisitorImpl(final HandlerRunner<H> runner, final HandlerStackManipulator<H> delegate) {
        this.runner = runner;
        prevH = null;
        nextH = null;
        this.delegate = delegate;
    }

    @Override
    public boolean onHandler(final H handler, final H prevH, final H nextH, final int handlerSeq) {
        this.prevH = prevH;
        this.nextH = nextH;
        this.handlerSeq = handlerSeq;
        return runner.run(handler, this);
    }

    @Override
    public void afterStackVisited() {
        for (Runnable r : handlerStackChanges) {
            r.run();
        }
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

    int getHandlerSeq() {
        return handlerSeq;
    }

    @Override
    public void addFirst(final H handler) {
        handlerStackChanges.add(new Runnable() {
            @Override
            public void run() {
                delegate.addFirst(handler);
            }
        });
    }

    @Override
    public void removeFirst() {
        handlerStackChanges.add(new Runnable() {
            @Override
            public void run() {
                delegate.removeFirst();
            }
        });
    }

    @Override
    public void removeAllPreceeding() {
        final int currentHandlerSeq = handlerSeq;
        handlerStackChanges.add(new Runnable() {
            @Override
            public void run() {
                delegate.removeFirst(currentHandlerSeq);        
            }
        });
    }
}
