package com.github.mhdirkse.codegen.runtime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public final class HandlerStack<H> {
    private Deque<H> handlers = new ArrayDeque<>();

    void addFirst(H handler) {
        handlers.addFirst(handler);
    }

    void removeFirst() {
        handlers.removeFirst();
    }

    void run(HandlerVisitor<H> handlerVisitor) {
        Iterator<H> it = handlers.iterator();
        boolean handled = false;
        while (it.hasNext() && !handled) {
            handled = handlerVisitor.onHandler(it.next());
        }
        if (!handled) {
            handled = handlerVisitor.onNoMoreHandlers();
        }
        if (!handled) {
            throw new NotHandledException(getHandlerNames());
        }
    }

    private String getHandlerNames() {
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
