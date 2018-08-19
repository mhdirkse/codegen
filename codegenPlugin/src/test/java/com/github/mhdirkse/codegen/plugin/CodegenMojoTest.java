package com.github.mhdirkse.codegen.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.VelocityContext;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;

public class CodegenMojoTest implements Logger {
    private final List<String> debugs = new ArrayList<>();
    private final List<String> infos = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private class TestableStub extends CodegenMojo.Testable {
        @Override
        Logger getLogger() {
            return CodegenMojoTest.this;
        }

        @Override
        String getProgram() {
            return "dummyProgram";
        }
    }

    private CodegenMojo.Testable testable;

    @Before
    public void setUp() {
        testable = new TestableStub();
    }

    @Override
    public void debug(final String msg) {
        debugs.add(msg);
    }

    @Override
    public void info(final String msg) {
        infos.add(msg);
    }

    @Override
    public void error(final String msg) {
        errors.add(msg);
    }

    @Override
    public void debug(final String msg, final Throwable e) {
        throw new IllegalArgumentException(msg, e);
    }

    @Override
    public void info(final String msg, final Throwable e) {
        throw new IllegalArgumentException(msg, e);
    }

    @Override
    public void error(final String msg, final Throwable e) {
        throw new IllegalArgumentException(msg, e);
    }

    @Test
    public void testPackageToRelativePathLinux() {
        Assert.assertEquals("base/com/github/mhdirkse/X.java", CodegenMojo.Testable.classToPathOfJavaFile(
                new File("base"), "com.github.mhdirkse.X").toString());
    }

    @Test
    public void testPackageToRelativePathLinuxNoPath() {
        Assert.assertEquals("base/X.java", CodegenMojo.Testable.classToPathOfJavaFile(
                new File("base"), "X").toString());
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

    @Test
    public void testWhenVelocityContextHasTargetClassModelWithFullNameThenTargetReturned() 
            throws MojoExecutionException {
        VelocityContext input = new VelocityContext();
        ClassModel cm = new ClassModel();
        cm.setFullName("SomeClass");
        input.put("target", cm);
        ClassModel actualTarget = testable.getTarget(input, "myField");
        Assert.assertNotNull(actualTarget);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testWhenVelocityContextMissesTargetThenError() {
        boolean gotException = false;
        VelocityContext input = new VelocityContext();
        try {
            testable.getTarget(input, "myField");
        }
        catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("myField"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("Cannot get output file name."));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("myField"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("\"target\""));
    }

    @Test
    public void testWhenVelocityContextTargetNotClassModelThenError() {
        boolean gotException = false;
        VelocityContext input = new VelocityContext();
        input.put("target", "Not a class model");
        try {
            testable.getTarget(input, "myField");
        }
        catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("myField"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("Expected a ClassModel as \"target\" in VelocityContext"));
    }

    @Test
    public void whenClassModelInVelocityContextNoFullNameThenError() {
        boolean gotException = false;
        VelocityContext input = new VelocityContext();
        ClassModel cm = new ClassModel();
        cm.setFullName("");
        input.put("target", cm);
        try {
            testable.getTarget(input, "myField");
        }
        catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("myField"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("ClassModel \"target\" does not have fullName"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("."));
    }
}
