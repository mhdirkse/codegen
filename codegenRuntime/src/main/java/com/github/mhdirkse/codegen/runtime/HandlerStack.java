package com.github.mhdirkse.codegen.runtime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public final class HandlerStack<H> implements HandlerStackManipulator<H> {
    private Deque<H> handlers = new ArrayDeque<>();

    @Override
    public void addFirst(H handler) {
        handlers.addFirst(handler);
    }

    @Override
    public void removeFirst() {
        handlers.removeFirst();
    }

    @Override
    public void removeFirst(int count) {
        for (int i = 0; i < count; ++i) {
            removeFirst();
        }
    }

    public void run(final HandlerRunner<H> runner) {
        run(new HandlerVisitorImpl<H>(runner, this));
    }

    void run(final HandlerVisitor<H> handlerVisitor) {
        try {
            new Visitor(handlerVisitor, handlers.iterator()).run();    
        } finally {
            handlerVisitor.afterStackVisited();
        }
    }

    private class Visitor {
        private final HandlerVisitor<H> handlerVisitor;
        private final Iterator<H> it;

        private H prevH = null;
        private H curH = null;
        private H nextH = null;
        private int handlerSeq = 0;

        private boolean handled = false;

        Visitor(
                final HandlerVisitor<H> handlerVisitor,
                final Iterator<H> it) {
            this.handlerVisitor = handlerVisitor;
            this.it = it;
        }

        void run() {
            while (it.hasNext() && !handled) {
                iterate(it.next());
            }
            if (!handled) {
                iterate(null);
            }
            if (!handled) {
                throw new NotHandledException(getHandlerNames());
            }
        }

        private void iterate(final H nextHandlerOrNull) {
            shift(nextHandlerOrNull);
            if (curH != null) {
                handled = callHandlerVisitor();
                ++handlerSeq;
            }
        }

        private void shift(H newHandler) {
            prevH = curH;
            curH = nextH;
            nextH = newHandler;
        }

        private boolean callHandlerVisitor() {
            return handlerVisitor.onHandler(curH, prevH, nextH, handlerSeq);
        }
    }

    String getHandlerNames() {
        if (handlers.isEmpty()) {
            return "No handlers";
        }
        return getHandlerNamesUnchecked();
    }

    private String getHandlerNamesUnchecked() {
        List<String> names = new ArrayList<>();
        for (H h : handlers) {
            names.add(h.toString());
        }
        return StringUtils.join(names, ", ");
    }
}
