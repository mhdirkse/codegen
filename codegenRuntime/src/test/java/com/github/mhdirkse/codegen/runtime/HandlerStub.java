package com.github.mhdirkse.codegen.runtime;

class HandlerStub {
    private final boolean returnHandled;
    private final String name;

    HandlerStub(final boolean returnHandled) {
        this.returnHandled = returnHandled;
        this.name = "unnamed";
    }

    HandlerStub(final boolean returnHandled, String name) {
        this.returnHandled = returnHandled;
        this.name = name;
    }

    public boolean handleMe(String someArgument, HandlerStackContext ctx) {
        return returnHandled;
    }

    @Override
    public String toString() {
        return name;
    }
}
