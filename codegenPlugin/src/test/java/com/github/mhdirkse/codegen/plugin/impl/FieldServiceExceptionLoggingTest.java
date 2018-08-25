package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import com.github.mhdirkse.codegen.compiletime.Input;

import org.junit.Assert;

public class FieldServiceExceptionLoggingTest extends LogTestBase {
    private FieldService instance;
    private Field inaccessibleField;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        StatusReportingService reporter = new StatusReportingServiceImpl(this);
        ServiceFactory factory = new ServiceFactoryImpl(new InputProgram(), reporter, null);
        instance = factory.fieldService();
        inaccessibleField = InputProgram.class.getDeclaredField("notAccessible");
    }

    static class InputProgram implements Runnable {
        @SuppressWarnings("unused")
        private String notAccessible;

        @Override
        public void run() {
        }
    }

    private class Callback implements FieldService.Callback {
        @Override
        public Status getStatusAccessModifierError(String modifier) {
            return notImplemented();
        }

        private Status notImplemented() {
            throw new IllegalStateException("Not implemented in this test.");
        }

        @Override
        public Status getStatusTypeMismatch(Class<?> actual) {
            return notImplemented();
        }

        @Override
        public Status getStatusFieldGetError() {
            return Status.forFieldError(
                    StatusCode.FIELD_GET_ERROR,
                    Input.class,
                    inaccessibleField);
        }

        @Override
        public Status getStatusFieldSetError() {
            return Status.forFieldError(
                    StatusCode.FIELD_SET_ERROR,
                    Input.class,
                    inaccessibleField);
        }

        @Override
        public Status getStatusFieldValueIsNull() {
            return notImplemented();
        }
    }

    @Test
    public void whenFieldCannotBeReadThenResultingExceptionIsLogged() {
        instance.getField(inaccessibleField, new Callback());
        Assert.assertEquals(1, errors.size());
        Assert.assertNotNull(errors.get(0).msg);
        Assert.assertNotNull(errors.get(0).e);
    }

    @Test
    public void whenFieldCannotBeWrittenThenResultingExceptionIsLogged() {
        instance.setField(inaccessibleField, "another string", new Callback());
        Assert.assertEquals(1, errors.size());
        Assert.assertNotNull(errors.get(0).msg);
        Assert.assertNotNull(errors.get(0).e);
    }
}
