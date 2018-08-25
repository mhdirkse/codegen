package com.github.mhdirkse.codegen.plugin.impl;

import java.util.ArrayList;
import java.util.List;

abstract class LogTestBase implements Logger {
    List<LogItem> debugs;
    List<LogItem> infos;
    List<LogItem> errors;

    class LogItem {
        String msg;
        Throwable e;

        LogItem(String msg, Throwable e) {
            this.msg = msg;
            this.e = e;
        }
    }

    public void setUp() throws Exception {
        debugs = new ArrayList<>();
        infos = new ArrayList<>();
        errors = new ArrayList<>();
    }

    @Override
    public void debug(final String msg) {
        debugs.add(new LogItem(msg, null));
    }

    @Override
    public void info(final String msg) {
        infos.add(new LogItem(msg, null));
    }

    @Override
    public void error(final String msg) {
        errors.add(new LogItem(msg, null));
    }

    @Override
    public void debug(final String msg, final Throwable e) {
        debugs.add(new LogItem(msg, e));
    }

    @Override
    public void info(final String msg, final Throwable e) {
        infos.add(new LogItem(msg, e));
    }

    @Override
    public void error(final String msg, final Throwable e) {
        errors.add(new LogItem(msg, e));
    }
}
