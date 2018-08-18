package com.github.mhdirkse.codegen.plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.mhdirkse.codegen.compiletime.Input;

public class FieldManipulationTest implements Logger {
    private static final String MESSAGE = "Some message";
    private static final IllegalArgumentException EXCEPTION = new IllegalArgumentException();

    private class LogItem {
        String msg;
        Throwable e;

        LogItem(String msg, Throwable e) {
            this.msg = msg;
            this.e = e;
        }
    }

    private List<LogItem> infos;
    private List<LogItem> errors;

    @Before
    public void setUp() {
        infos = new ArrayList<>();
        errors = new ArrayList<>();
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
    public void info(String msg, Throwable e) {
        infos.add(new LogItem(msg, e));
    }

    @Override
    public void error(final String msg, final Throwable e) {
        errors.add(new LogItem(msg, e));
    }

    private static class TestInput {
        @SuppressWarnings("unused")
        public int aField;
    }

    private enum Priority {INFO, ERROR};

    private static class FieldManipulationStub extends FieldManipulation {
        final boolean result;
        final Priority priority;
        final Exception toThrow;

        FieldManipulationStub(
                final Class<? extends Annotation> annotation,
                final Field field,
                final Logger logger,
                final boolean result,
                final Priority priority,
                final Exception toThrow) {
            super(annotation, field, logger);
            this.result = result;
            this.priority = priority;
            this.toThrow = toThrow;
        }

        @Override
        public boolean runImpl() throws Exception {
            if(toThrow == null) {
                if(priority == Priority.ERROR) {
                    error(MESSAGE);
                } else {
                    info(MESSAGE);
                }
            } else {
                if(priority == Priority.ERROR) {
                    error(MESSAGE, toThrow);
                } else {
                    info(MESSAGE, toThrow);
                }
            }
            if(toThrow != null) {
                throw toThrow;
            }
            return result;
        }
    }

    @Test
    public void testLogLineFormatInfo() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                Input.class,
                TestInput.class.getField("aField"),
                this,
                true,
                Priority.INFO,
                null);
        instance.info(MESSAGE);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(1, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, infos.get(0).msg);
        Assert.assertNull(infos.get(0).e);
    }

    @Test
    public void testLogLineFormatError() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                Input.class,
                TestInput.class.getField("aField"),
                this,
                true,
                Priority.ERROR,
                null);
        instance.error(MESSAGE);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(0, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, errors.get(0).msg);
        Assert.assertNull(errors.get(0).e);
    }

    @Test
    public void testLogLineFormatInfoWithException() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                Input.class,
                TestInput.class.getField("aField"),
                this,
                true,
                Priority.INFO,
                EXCEPTION);
        instance.info(MESSAGE, EXCEPTION);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(1, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, infos.get(0).msg);
        Assert.assertEquals(EXCEPTION, infos.get(0).e);
    }

    @Test
    public void testLogLineFormatErrorWithException() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                Input.class,
                TestInput.class.getField("aField"),
                this,
                true,
                Priority.ERROR,
                EXCEPTION);
        instance.error(MESSAGE, EXCEPTION);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(0, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, errors.get(0).msg);
        Assert.assertEquals(EXCEPTION, errors.get(0).e);
    }

    @Test
    public void whenRunImplReturnsTrueThenRunReturnsTrue() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                Input.class,
                TestInput.class.getField("aField"),
                this,
                true,
                Priority.ERROR,
                null);
        Assert.assertTrue(instance.run());
    }

    @Test
    public void whenRunImplReturnsFalseThenRunReturnsFalse() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                Input.class,
                TestInput.class.getField("aField"),
                this,
                false,
                Priority.ERROR,
                null);
        Assert.assertFalse(instance.run());
    }
}
