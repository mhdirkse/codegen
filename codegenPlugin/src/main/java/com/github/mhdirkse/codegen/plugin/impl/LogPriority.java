package com.github.mhdirkse.codegen.plugin.impl;

import java.util.function.BiConsumer;

enum LogPriority {
    DEBUG(Logger::debug, Logger::debug),
    INFO(Logger::info, Logger::info),
    ERROR(Logger::error, Logger::error);

    @FunctionalInterface
    private interface TriConsumer<U, V, W> {
        void accept(U u, V v, W w);
    }

    LogPriority(
            final BiConsumer<Logger, String> logMethod,
            final TriConsumer<Logger, String, Throwable> logMethodException) {
        this.logMethod = logMethod;
        this.logMethodException = logMethodException;
    }

    private final BiConsumer<Logger, String> logMethod;
    private final TriConsumer<Logger, String, Throwable> logMethodException;

    void log(final String msg, final Logger logger) {
        logMethod.accept(logger, msg);
    }

    void log(final String msg, final Throwable e, final Logger logger) {
        logMethodException.accept(logger, msg, e);
    }
}
