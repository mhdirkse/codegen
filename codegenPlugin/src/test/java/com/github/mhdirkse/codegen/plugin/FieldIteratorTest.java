package com.github.mhdirkse.codegen.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.VelocityContext;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;

public class FieldIteratorTest implements Logger {
    private class LogItem {
        String msg;
        @SuppressWarnings("unused")
        Throwable e;

        LogItem(String msg, Throwable e) {
            this.msg = msg;
            this.e = e;
        }
    }

    private List<LogItem> infos;
    private List<LogItem> errors;
    private List<LogItem> debugs;

    private FieldIterator.OutputPopulator instance;

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
    public void testWhenAllOutputsAreVelocityContextThenOutputsFound() throws MojoExecutionException {
        instance = new FieldIterator.OutputPopulator(
                new TestProgramHappy(), this);
        List<Optional<FieldManipulation>> manipulations = instance.run();
        Assert.assertEquals(1, manipulations.size());
        for(Optional<FieldManipulation> manipulation : manipulations) {
            String actual = manipulation.get().f.getName();
            Assert.assertEquals("x", actual);
        }
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testWhenOutputIsPrivateThenNotFoundAndErrorLogged() {
        instance = new FieldIterator.OutputPopulator(
                new TestProgramOutputIsNotPublic(), this);
        List<Optional<FieldManipulation>> manipulations = instance.run();
        Assert.assertEquals(1,  manipulations.size());
        Assert.assertEquals(0, getNumPresent(manipulations));
        Assert.assertEquals(1, errors.size());
        String actualError = errors.get(0).msg;
        Assert.assertThat(actualError, CoreMatchers.containsString("not public"));
        Assert.assertThat(actualError, CoreMatchers.containsString("xyz"));
    }

    private long getNumPresent(List<Optional<FieldManipulation>> manipulations) {
        return manipulations.stream().filter(Optional::isPresent).count();
    }

    @Test
    public void testWhenOutputFieldNotVelocityContextThenNotFoundAndErrorLogged() {
        instance = new FieldIterator.OutputPopulator(
                new TestProgramOutputNotVelocityContext(), this);
        List<Optional<FieldManipulation>> manipulations = instance.run();
        Assert.assertEquals(1,  manipulations.size());
        Assert.assertEquals(0, getNumPresent(manipulations));
        Assert.assertEquals(1, errors.size());
        String actualError = errors.get(0).msg;        
        Assert.assertThat(actualError, CoreMatchers.containsString("not VelocityContext"));
        Assert.assertThat(actualError, CoreMatchers.containsString("xyz"));
    }

    @Test
    public void testWhenOutputFieldNotVelocityContextAndNotPublicThenNotFoundAndErrorLogged() {
        instance = new FieldIterator.OutputPopulator(
                new TestProgramOutputAllWrong(), this);
        List<Optional<FieldManipulation>> manipulations = instance.run();
        Assert.assertEquals(1,  manipulations.size());
        Assert.assertEquals(0, getNumPresent(manipulations));
        Assert.assertEquals(1, errors.size());
        String actualError = errors.get(0).msg;        
        Assert.assertThat(actualError, CoreMatchers.containsString("not VelocityContext"));
        Assert.assertThat(actualError, CoreMatchers.containsString("not public"));
        Assert.assertThat(actualError, CoreMatchers.containsString("xyz"));
    }

    class TestProgramHappy implements Runnable {
        @Input("MyClass")
        public ClassModel i;

        @Output("templateForX")
        public VelocityContext x;

        @Override
        public void run() {
        }
    }

    class TestProgramOutputIsNotPublic implements Runnable {
        @Output("templateForX")
        VelocityContext xyz;

        @Override
        public void run() {
        }
    }

    class TestProgramOutputNotVelocityContext implements Runnable {
        @Output("templateForX")
        public String xyz;

        @Override
        public void run() {
        }
    }

    class TestProgramOutputAllWrong implements Runnable {
        @Output("templateForX")
        String xyz;

        @Override
        public void run() {
        }
    }
}
