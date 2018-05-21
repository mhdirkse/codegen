package com.github.mhdirkse.codegen.runtime;

public class NotHandledException extends IllegalArgumentException {
    private static final long serialVersionUID = 3467081529268548888L;

    public NotHandledException(final String message) {
        super(message);
    }

    public NotHandledException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
