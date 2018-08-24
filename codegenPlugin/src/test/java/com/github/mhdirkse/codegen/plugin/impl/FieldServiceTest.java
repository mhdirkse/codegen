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
public class FieldServiceTest {
    private FieldService instance;
    private StatusReportingServiceStub statusReportingService;

    @Mock
    private FieldService.Callback callback;

    @Before
    public void setUp() {
        statusReportingService = new StatusReportingServiceStub();
        instance = new ServiceFactoryImpl(new TestInput(), statusReportingService, null)
                .fieldService();
    }

    @SuppressWarnings("unused")
    private static class TestInput implements Runnable {
        public final String finalField = "finalField";
        public static String staticField = "staticField";
        public String normalField;

        @Override
        public void run() {
        }
    }

    @Test
    public void whenFieldIsNotFinalThenCheckNotFinalSucceeds() throws NoSuchFieldException {
        replay(callback);
        Assert.assertTrue(instance.checkNotFinal(TestInput.class.getField("staticField"), callback));
        verify(callback);
        Assert.assertEquals(0, statusReportingService.getStatusses().size());
    }

    @Test
    public void whenFieldIsFinalThenCheckNotFinalFails() throws NoSuchFieldException {
        Field field = TestInput.class.getField("finalField");
        expect(callback.getStatusAccessModifierError("final")).andReturn(
                Status.forFieldError(
                        StatusCode.FIELD_UNWANTED_ACCESS_MODIFIER,
                        Input.class, field, "final"));
        replay(callback);
        Assert.assertFalse(instance.checkNotFinal(field, callback));
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
        Assert.assertTrue(instance.checkNotStatic(TestInput.class.getField("finalField"), callback));
        verify(callback);
        Assert.assertEquals(0, statusReportingService.getStatusses().size());
    }

    @Test
    public void whenFieldIsStaticThenCheckNotStaticFails() throws NoSuchFieldException {
        Field field = TestInput.class.getField("staticField");
        expect(callback.getStatusAccessModifierError("static")).andReturn(
                Status.forFieldError(
                        StatusCode.FIELD_UNWANTED_ACCESS_MODIFIER,
                        Input.class, field, "static"));
        replay(callback);
        Assert.assertFalse(instance.checkNotStatic(field, callback));
        verify(callback);
        Assert.assertEquals(1, statusReportingService.getStatusses().size());
        Status actualStatus = statusReportingService.getStatusses().get(0);
        Assert.assertEquals(StatusCode.FIELD_UNWANTED_ACCESS_MODIFIER, actualStatus.getStatusCode());
        Assert.assertEquals(LogPriority.ERROR, actualStatus.getLogPriority());
        Assert.assertArrayEquals(
                new String[] {"Input", "staticField", "static"},
                actualStatus.getArguments());
    }

    @Test
    public void whenTypeMatchesThenCheckTypeSucceeds() throws NoSuchFieldException {
        Field field = TestInput.class.getField("finalField");
        replay(callback);
        Assert.assertTrue(instance.checkType(field, String.class, callback));
        verify(callback);
        Assert.assertEquals(0, statusReportingService.getStatusses().size());
    }

    @Test
    public void whenTypeMismatchThenCheckTypeGivesError() throws NoSuchFieldException {
        Field field = TestInput.class.getField("finalField");
        expect(callback.getStatusTypeMismatch(String.class)).andReturn(
                Status.forFieldError(StatusCode.FIELD_TYPE_MISMATCH,
                        Override.class, field, "dummy", "dummy"));
        replay(callback);
        Assert.assertFalse(instance.checkType(field, Integer.class, callback));
        Assert.assertEquals(1, statusReportingService.getStatusses().size());
        Assert.assertEquals(
                StatusCode.FIELD_TYPE_MISMATCH,
                statusReportingService.getStatusses().get(0).getStatusCode());
    }

    @Test
    public void testFieldServiceSetAndGetField() throws NoSuchFieldException {
        Field field = TestInput.class.getField("normalField");
        instance.setField(field, "some string", callback);
        String retrieved = instance.getField(field, callback).map(v -> String.class.cast(v)).get();
        Assert.assertEquals("some string", retrieved);
    }
}
