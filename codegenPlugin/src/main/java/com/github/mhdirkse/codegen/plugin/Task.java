package com.github.mhdirkse.codegen.plugin;

public class Task {
    private String source;
    private String handler;
    private String abstractHandler;
    private String delegator;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public final String getAbstractHandler() {
        return abstractHandler;
    }

    public final void setAbstractHandler(String abstractHandler) {
        this.abstractHandler = abstractHandler;
    }

    public final String getDelegator() {
        return delegator;
    }

    public final void setDelegator(String delegator) {
        this.delegator = delegator;
    }
}
