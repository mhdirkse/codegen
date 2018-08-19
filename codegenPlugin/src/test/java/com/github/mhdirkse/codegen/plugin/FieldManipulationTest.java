package com.github.mhdirkse.codegen.plugin;

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

    private List<LogItem> debugs;
    private List<LogItem> infos;
    private List<LogItem> errors;

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

    private static class TestInput {
        @SuppressWarnings("unused")
        public int aField;
    }

    private static class FieldManipulationStub extends FieldManipulation {
        final boolean result;
        final Exception toThrow;

        FieldManipulationStub(
                final Logger logger,
                final boolean result,
                final Exception toThrow) {
            super(
                    Input.class,
                    getSuperInputField(),
                    logger);
            this.result = result;
            this.toThrow = toThrow;
        }

        private static Field getSuperInputField() {
            try {
                return TestInput.class.getField("aField");
            } catch(NoSuchFieldException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public boolean runImpl() throws Exception {
            if(toThrow == null) {
                info(MESSAGE);
            } else {
                info(MESSAGE, toThrow);
            }
            if(toThrow != null) {
                throw toThrow;
            }
            return result;
        }
    }

    @Test
    public void testLogLineFormatDebug() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                this,
                true,
                null);
        instance.debug(MESSAGE);
        Assert.assertEquals(1, debugs.size());
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(0, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, debugs.get(0).msg);
        Assert.assertNull(debugs.get(0).e);
    }

    @Test
    public void testLogLineFormatInfo() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                this,
                true,
                null);
        instance.info(MESSAGE);
        Assert.assertEquals(0, debugs.size());
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(1, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, infos.get(0).msg);
        Assert.assertNull(infos.get(0).e);
    }

    @Test
    public void testLogLineFormatError() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                this,
                true,
                null);
        instance.error(MESSAGE);
        Assert.assertEquals(0, debugs.size());
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(0, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, errors.get(0).msg);
        Assert.assertNull(errors.get(0).e);
    }

    @Test
    public void testLogLineFormatDebugWithException() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                this,
                true,
                EXCEPTION);
        instance.debug(MESSAGE, EXCEPTION);
        Assert.assertEquals(1, debugs.size());
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(0, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, debugs.get(0).msg);
        Assert.assertEquals(EXCEPTION, debugs.get(0).e);
    }

    @Test
    public void testLogLineFormatInfoWithException() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                this,
                true,
                EXCEPTION);
        instance.info(MESSAGE, EXCEPTION);
        Assert.assertEquals(0, debugs.size());
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(1, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, infos.get(0).msg);
        Assert.assertEquals(EXCEPTION, infos.get(0).e);
    }

    @Test
    public void testLogLineFormatErrorWithException() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                this,
                true,
                EXCEPTION);
        instance.error(MESSAGE, EXCEPTION);
        Assert.assertEquals(0, debugs.size());
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(0, infos.size());
        Assert.assertEquals("@Input aField: " + MESSAGE, errors.get(0).msg);
        Assert.assertEquals(EXCEPTION, errors.get(0).e);
    }

    @Test
    public void whenRunImplReturnsTrueThenRunReturnsTrue() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                this,
                true,
                null);
        Assert.assertTrue(instance.run());
    }

    @Test
    public void whenRunImplReturnsFalseThenRunReturnsFalse() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                this,
                false,
                null);
        Assert.assertFalse(instance.run());
    }

    @Test
    public void whenRunImplThrowsThenRunReturnsFalse() throws Exception {
        FieldManipulation instance = new FieldManipulationStub(
                this,
                true,
                EXCEPTION);
        Assert.assertFalse(instance.run());
    }
}
