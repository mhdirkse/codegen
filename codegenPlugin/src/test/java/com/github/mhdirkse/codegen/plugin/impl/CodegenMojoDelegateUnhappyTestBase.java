package com.github.mhdirkse.codegen.plugin.impl;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

public abstract class CodegenMojoDelegateUnhappyTestBase {
    @Parameter(0)
    public String fieldName;

    @Parameter(1)
    public StatusCode expectedStatusCode;

    Map<String, List<StatusSummary>> actualStatusses;

    public void setUp(final Runnable program) {
        StatusReportingServiceStub reporter = new StatusReportingServiceStub();
        ServiceFactory sf = new ServiceFactoryImpl(program, reporter, this.getClass().getClassLoader());
        CodegenMojoDelegate instance = new CodegenMojoDelegate(program, sf);
        runCodegenMojoDelegate(instance);
        actualStatusses = StatusSummary.getSummary(reporter.getStatusses());
    }

    abstract void runCodegenMojoDelegate(CodegenMojoDelegate instance);
    
    @Test
    public void onlyExpectedErrorSeen() {
        Assert.assertEquals(1, actualStatusses.get(fieldName).size());
    }

    @Test
    public void statusCodeMatches() {
        Assert.assertEquals(expectedStatusCode, actualStatusses.get(fieldName).get(0).statusCode);
    }
}
