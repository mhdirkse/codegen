package com.github.mhdirkse.codegen.plugin.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.github.mhdirkse.codegen.plugin.impl.LogPriority.DEBUG;
import static com.github.mhdirkse.codegen.plugin.impl.LogPriority.INFO;
import static com.github.mhdirkse.codegen.plugin.impl.LogPriority.ERROR;

public class LogPriorityTest extends LogTestBase {
    private static final String MSG = "msg";
    private static final Exception E = new IllegalStateException();


    @Before
    public void setUp() {
        super.setUp();
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
