package com.github.mhdirkse.codegen.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.VelocityContext;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.CodegenProgram;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;

public class CodegenMojoTest implements Logger {
    private final List<String> infos = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

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
        Assert.assertEquals("base/com/github/mhdirkse/X.java", CodegenMojo.classToPathOfJavaFile(
                new File("base"), "com.github.mhdirkse.X").toString());
    }

    @Test
    public void testPackageToRelativePathLinuxNoPath() {
        Assert.assertEquals("base/X.java", CodegenMojo.classToPathOfJavaFile(
                new File("base"), "X").toString());
    }

    @Test
    public void testWhenAllOutputsAreVelocityContextThenOutputsFound() throws MojoExecutionException {
        Set<Field> fields = CodegenMojo.getOutputFields(new TestProgramHappy(), this);
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
            CodegenMojo.getOutputFields(new TestProgramOutputIsNotPublic(), this);
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
            CodegenMojo.getOutputFields(new TestProgramOutputNotVelocityContext(), this);
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
            CodegenMojo.getOutputFields(new TestProgramOutputAllWrong(), this);
        } catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("not VelocityContext and not public"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("xyz"));
    }

    class TestProgramHappy implements CodegenProgram {
        @Input("MyClass")
        public ClassModel i;

        @Output("templateForX")
        public VelocityContext x;

        @Override
        public void run() {
        }
    }

    class TestProgramOutputIsNotPublic implements CodegenProgram {
        @Output("templateForX")
        VelocityContext xyz;

        @Override
        public void run() {
        }
    }

    class TestProgramOutputNotVelocityContext implements CodegenProgram {
        @Output("templateForX")
        public String xyz;

        @Override
        public void run() {
        }
    }

    class TestProgramOutputAllWrong implements CodegenProgram {
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
        ClassModel actualTarget = CodegenMojo.getTarget(input, "myField", this);
        Assert.assertNotNull(actualTarget);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testWhenVelocityContextMissesTargetThenError() {
        boolean gotException = false;
        VelocityContext input = new VelocityContext();
        try {
            CodegenMojo.getTarget(input, "myField", this);
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
            CodegenMojo.getTarget(input, "myField", this);
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
        Set<Field> fields = CodegenMojo.getInputFields(new TestProgramHappy(), this);
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
            CodegenMojo.getInputFields(new TestProgramInputNotPublic(), this);
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
            CodegenMojo.getInputFields(new TestProgramInputNotClassModel(), this);
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
            CodegenMojo.getInputFields(new TestProgramInputMultipleErrors(), this);
        }
        catch(MojoExecutionException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
        Assert.assertEquals(1, errors.size());
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("myInput"));
        Assert.assertThat(errors.get(0), CoreMatchers.containsString("not ClassModel and not public"));
    }

    private static class TestProgramInputNotPublic implements CodegenProgram {
        @Override
        public void run() {
        }

        @Input("MyClass")
        ClassModel myInput;
    }

    private static class TestProgramInputNotClassModel implements CodegenProgram {
        @Override
        public void run() {
        }

        @Input("MyClass")
        public String myInput;
    }

    private static class TestProgramInputMultipleErrors implements CodegenProgram {
        @Override
        public void run() {
        }

        @Input("MyClass")
        String myInput;
    }
}
