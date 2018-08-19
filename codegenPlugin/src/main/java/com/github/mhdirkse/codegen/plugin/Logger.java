package com.github.mhdirkse.codegen.plugin;

public interface Logger {
    void debug(String msg);
    void info(String msg);
    void error(String msg);
    void debug(String msg, Throwable e);
    void info(String msg, Throwable e);
    void error(String msg, Throwable e);
}
