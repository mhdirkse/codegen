package com.github.mhdirkse.codegen.plugin.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.lang.reflect.Field;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.mhdirkse.codegen.compiletime.Input;

@RunWith(EasyMockRunner.class)
public class AccessModifierCheckServiceTest {
    private AccessModifierCheckService instance;
    private StatusReportingServiceStub statusReportingService;

    @Mock
    private AccessModifierCheckService.Callback callback;

    @Before
    public void setUp() {
        statusReportingService = new StatusReportingServiceStub();
        instance = new ServiceFactory(new TestInput(), statusReportingService)
                .accessModifierChecker(callback);
    }

    @SuppressWarnings("unused")
    private static class TestInput implements Runnable {
        public final String finalField = "finalField";
        public static String staticField = "staticField";

        @Override
        public void run() {
        }
    }

    @Test
    public void whenFieldIsNotFinalThenCheckNotFinalSucceeds() throws NoSuchFieldException {
        replay(callback);
        instance.checkNotFinal(TestInput.class.getField("staticField"));
        verify(callback);
    }

    @Test
    public void whenFieldIsFinalThenCheckNotFinalFails() throws NoSuchFieldException {
        Field field = TestInput.class.getField("finalField");
        expect(callback.getStatusAccessModifierError("final")).andReturn(
                Status.forFieldError(
                        StatusCode.FIELD_UNWANTED_ACCESS_MODIFIER,
                        Input.class, field, "final"));
        replay(callback);
        instance.checkNotFinal(field);
        verify(callback);
        Assert.assertEquals(1, statusReportingService.getStatusses().size());
        Status actualStatus = statusReportingService.getStatusses().get(0);
        Assert.assertEquals(StatusCode.FIELD_UNWANTED_ACCESS_MODIFIER, actualStatus.getStatusCode());
        Assert.assertEquals(LogPriority.ERROR, actualStatus.getLogPriority());
        Assert.assertArrayEquals(
                new String[] {"Input", "finalField", "final"},
                actualStatus.getArguments());
    }

    @Test
    public void whenFieldIsNotStaticThenCheckNotStaticSucceeds() throws NoSuchFieldException {
        replay(callback);
        instance.checkNotStatic(TestInput.class.getField("finalField"));
        verify(callback);
    }

    @Test
    public void whenFieldIsStaticThenCheckNotStaticFails() throws NoSuchFieldException {
        Field field = TestInput.class.getField("staticField");
        expect(callback.getStatusAccessModifierError("static")).andReturn(
                Status.forFieldError(
                        StatusCode.FIELD_UNWANTED_ACCESS_MODIFIER,
                        Input.class, field, "static"));
        replay(callback);
        instance.checkNotStatic(field);
        verify(callback);
        Assert.assertEquals(1, statusReportingService.getStatusses().size());
        Status actualStatus = statusReportingService.getStatusses().get(0);
        Assert.assertEquals(StatusCode.FIELD_UNWANTED_ACCESS_MODIFIER, actualStatus.getStatusCode());
        Assert.assertEquals(LogPriority.ERROR, actualStatus.getLogPriority());
        Assert.assertArrayEquals(
                new String[] {"Input", "staticField", "static"},
                actualStatus.getArguments());
    }
}
