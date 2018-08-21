package com.github.mhdirkse.codegen.plugin.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StatusReportingServiceTest extends LogTestBase {
    private StatusReportingService instance;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        instance = new StatusReportingServiceImpl(this);
    }

    @Test
    public void testWhenErrorThenHasErrorsTrueAndErrorLogged() {
        Assert.assertFalse(instance.hasErrors());
        instance.report(Status.general(StatusCode.TEST_STATUS_TWO_ARGS, LogPriority.ERROR, "one", "two"));
        Assert.assertTrue(instance.hasErrors());
        Assert.assertEquals(0, debugs.size());
        Assert.assertEquals(0, infos.size());
        Assert.assertEquals("Some status about: one and two.", errors.get(0).msg);
    }

    @Test
    public void testWhenInfoThenNotHasErrorsAndInfoLogged() {
        Assert.assertFalse(instance.hasErrors());
        instance.report(Status.general(StatusCode.TEST_STATUS_TWO_ARGS, LogPriority.INFO, "one", "two"));
        Assert.assertFalse(instance.hasErrors());
        Assert.assertEquals(0, debugs.size());
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals("Some status about: one and two.", infos.get(0).msg);
    }

    @Test
    public void testWhenErrorFollowedByInfoThenErrorNotCleared() {
        instance.report(Status.general(StatusCode.TEST_STATUS_ZERO_ARGS, LogPriority.ERROR));
        Assert.assertTrue(instance.hasErrors());
        instance.report(Status.general(StatusCode.TEST_STATUS_ZERO_ARGS, LogPriority.INFO));
        Assert.assertTrue(instance.hasErrors());
    }
}
