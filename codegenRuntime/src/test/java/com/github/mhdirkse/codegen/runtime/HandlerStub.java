package com.github.mhdirkse.codegen.runtime;

class HandlerStub {
    private final boolean returnHandled;
    private final String name;
    private final HandlerStub newHandler;

    HandlerStub(final boolean returnHandled) {
        this.returnHandled = returnHandled;
        this.name = "unnamed";
        this.newHandler = null;
    }

    HandlerStub(final boolean returnHandled, String name) {
        this.returnHandled = returnHandled;
        this.name = name;
        this.newHandler = null;
    }

    HandlerStub(final boolean returnHandled, String name, HandlerStub newHandler) {
        this.returnHandled = returnHandled;
        this.name = name;
        this.newHandler = newHandler;
    }

    public boolean handleMe(String someArgument, HandlerStackContext<HandlerStub> ctx) {
        if (newHandler != null) {
            ctx.addFirst(newHandler);
        }
        return returnHandled;
    }

    @Override
    public String toString() {
        return name;
    }
}
