package com.github.mhdirkse.codegen.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.VelocityContext;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;

public class CodegenMojoTest implements Logger {
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
    public void info(final String msg) {
        infos.add(msg);
    }

    @Override
    public void error(final String msg) {
        errors.add(msg);
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

    @Test
    public void testWhenAllOutputsAreVelocityContextThenOutputsFound() throws MojoExecutionException {
        Set<Field> fields = testable.getOutputFields(new TestProgramHappy());
        Assert.assertEquals(1, fields.size());
        for(Field field : fields) {
            String actual = field.getName();
            Assert.assertEquals("x", actual);
        }
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testWhenOutputIsPrivateThenNotFoundAndErrorLogged() {
        boolean gotException = false;
        try {
            testable.getOutputFields(new TestProgramOutputIsNotPublic());
        } catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("not public"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("xyz"));
    }

    @Test
    public void testWhenOutputFieldNotVelocityContextThenNotFoundAndErrorLogged() {
        boolean gotException = false;
        try {
            testable.getOutputFields(new TestProgramOutputNotVelocityContext());
        } catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("not VelocityContext"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("xyz"));
    }

    @Test
    public void testWhenOutputFieldNotVelocityContextAndNotPublicThenNotFoundAndErrorLogged() {
        boolean gotException = false;
        try {
            testable.getOutputFields(new TestProgramOutputAllWrong());
        } catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("not VelocityContext and not public"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("xyz"));
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
    public void testWhenVelocityContextHasTargetClassModelThenTargetReturned() 
            throws MojoExecutionException {
        VelocityContext input = new VelocityContext();
        input.put("target", new ClassModel());
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
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("Cannot get output file from VelocityContext"));
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
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("Expected ClassModel as target in VelocityContext"));
    }

    @Test
    public void testWhenProgramHasValidInputThenInputReturned() throws MojoExecutionException {
        Set<Field> fields = testable.getInputFields(new TestProgramHappy());
        Assert.assertEquals(1, fields.size());
        for (Field field : fields) {
            Assert.assertEquals("i", field.getName());
        }
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testWhenInputNotPublicThenError() {
        boolean gotException = false;
        try {
            testable.getInputFields(new TestProgramInputNotPublic());
        }
        catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("myInput"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("not public"));
    }

    @Test
    public void testWhenInputNotClassModelThenError() {
        boolean gotException = false;
        try {
            testable.getInputFields(new TestProgramInputNotClassModel());
        }
        catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("myInput"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("not ClassModel"));
    }

    @Test
    public void testWhenInputMultipleErrorsThenError() {
        boolean gotException = false;
        try {
            testable.getInputFields(new TestProgramInputMultipleErrors());
        }
        catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("myInput"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("not ClassModel and not public"));
    }

    private static class TestProgramInputNotPublic implements Runnable {
        @Override
        public void run() {
        }

        @Input("MyClass")
        ClassModel myInput;
    }

    private static class TestProgramInputNotClassModel implements Runnable {
        @Override
        public void run() {
        }

        @Input("MyClass")
        public String myInput;
    }

    private static class TestProgramInputMultipleErrors implements Runnable {
        @Override
        public void run() {
        }

        @Input("MyClass")
        String myInput;
    }

    @Test(expected = MojoExecutionException.class)
    public void testWhenClassNotFoundThenMojoExecutionException() throws MojoExecutionException {
        testable.getClass("InvalidClass", ClassLoaderAdapter.forCl(this.getClass().getClassLoader()));
    }

    @Test
    public void testGetHierarchyGivesChildrenAndParent() throws MojoExecutionException {
        ClassModelList actual = testable.getHierarchy(
                Parent.class,
                ClassLoaderAdapter.forCl(Parent.class.getClassLoader()));
        Assert.assertEquals(2, actual.size());
        Assert.assertThat(getSimpleNames(actual), CoreMatchers.hasItems(
                "Parent", "Child"));
    }

    private List<String> getSimpleNames(Collection<ClassModel> classModels) {
        return classModels.stream()
                .map(ClassModel::getSimpleName)
                .map(s -> s.split("\\$")[1])
                .collect(Collectors.toList());
    }

    private class Parent {
    }

    @SuppressWarnings("unused")
    private class Child extends Parent {
    }
}
