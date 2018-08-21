package com.github.mhdirkse.codegen.plugin.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.github.mhdirkse.codegen.plugin.impl.LogPriority.DEBUG;
import static com.github.mhdirkse.codegen.plugin.impl.LogPriority.INFO;
import static com.github.mhdirkse.codegen.plugin.impl.LogPriority.ERROR;

public class LogPriorityTest implements Logger {
    private static final String MSG = "msg";
    private static final Exception E = new IllegalStateException();

    private List<LogItem> debugs;
    private List<LogItem> infos;
    private List<LogItem> errors;

    private class LogItem {
        String msg;
        Throwable e;

        LogItem(String msg, Throwable e) {
            this.msg = msg;
            this.e = e;
        }
    }

    @Before
    public void setUp() {
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

    @Test
    public void whenDebugLogThenMessageLoggedAsDebug() {
        DEBUG.log(MSG, this);
        Assert.assertEquals(MSG, debugs.get(0).msg);
        Assert.assertNull(debugs.get(0).e);
        Assert.assertEquals(0, infos.size());
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void whenInfoLogThenMessageLoggedAsInfo() {
        INFO.log(MSG, this);
        Assert.assertEquals(MSG, infos.get(0).msg);
        Assert.assertNull(infos.get(0).e);
        Assert.assertEquals(0, debugs.size());
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void whenErrorLogWithExceptionThenMessageWithExceptionLoggedAsError() {
        ERROR.log(MSG, E, this);
        Assert.assertEquals(MSG, errors.get(0).msg);
        Assert.assertEquals(E, errors.get(0).e);
        Assert.assertEquals(0, debugs.size());
        Assert.assertEquals(0, infos.size());
    }
}
