package com.github.mhdirkse.codegen.runtime;

import java.util.ArrayList;
import java.util.List;

final class HandlerVisitorImpl<H>
implements HandlerVisitor<H>, HandlerStackContext<H>, HandlerStackChangeDefinition.Visitor<H> {
    private final HandlerRunner<H> runner;

    private H prevH;
    private H nextH;
    private int handlerSeq;

    private final HandlerStackManipulator<H> delegate;

    private List<HandlerStackChangeDefinition<H>> handlerStackChanges = new ArrayList<>();

    private int numPreceedingOffset = 0;

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
        numPreceedingOffset = 0;
        for (HandlerStackChangeDefinition<H> ch : handlerStackChanges) {
            ch.accept(this);
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
        handlerStackChanges.add(new HandlerStackChangeDefinition.AddFirst<H>(handlerSeq, handler));
    }

    @Override
    public void removeFirst() {
        handlerStackChanges.add(new HandlerStackChangeDefinition.RemoveFirst<H>(handlerSeq));
    }

    @Override
    public void removeAllPreceeding() {
        handlerStackChanges.add(new HandlerStackChangeDefinition.RemoveAllPreceeding<H>(handlerSeq));
    }

    @Override
    public void addFirstCmdExec(final int atSeq, final H handler) {
        delegate.addFirst(handler);
        numPreceedingOffset += 1;
    }

    @Override
    public void removeFirstCmdExec(final int atSeq) {
        delegate.removeFirst();
        numPreceedingOffset -= 1;
    }

    @Override
    public void removeAllPreceedingCmdExec(final int atSeq) {
        int remainingNumPreceeding = atSeq + numPreceedingOffset;
        if (remainingNumPreceeding >= 0) {
            removeAllPreceedingUnchecked(atSeq, remainingNumPreceeding);
        }
    }

    private void removeAllPreceedingUnchecked(final int atSeq, int remainingNumPreceeding) {
        delegate.removeFirst(remainingNumPreceeding);
        numPreceedingOffset = (-atSeq);
    }
}
