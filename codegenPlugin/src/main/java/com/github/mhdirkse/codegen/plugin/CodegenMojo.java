package com.github.mhdirkse.codegen.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.reflections.ReflectionUtils;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.github.mhdirkse.codegen.annotations.Input;
import com.github.mhdirkse.codegen.annotations.Output;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;
import com.github.mhdirkse.codegen.plugin.model.MethodModel;

/**
 * Goal which generates .java files from POJO description files.
 */
@Mojo(name = "codegen", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class CodegenMojo extends AbstractMojo implements Logger {
    @Component
    private MavenProject project;

    @Component
    private PluginDescriptor descriptor;

    @Component
    VelocityComponent velocityComponent;

    @Component
    private BuildContext buildContext;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/codegen")
    private File outputDirectory;

    @Parameter(defaultValue = "${project.build.sourceEncoding}")
    private String sourceEncoding;

    @Parameter
    public String program;

    @Override
    public void info(final String msg) {
        getLog().info(msg);
    }

    @Override
    public void error(final String msg) {
        getLog().error(msg);
    }

    public void execute() throws MojoExecutionException {
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath().toString());
        CodegenProgram instantiatedProgram = instantiate(program, CodegenProgram.class);
        ClassRealm realm = getClassRealm();
        populateProgramInputs(instantiatedProgram, realm);
        populateOutputFields(instantiatedProgram);
        instantiatedProgram.run();
        createOutputFiles(instantiatedProgram);
    }

    private <T> T instantiate(final String className, final Class<T> type) throws MojoExecutionException {
        try{
            return type.cast(type.getClassLoader().loadClass(className).newInstance());
        } catch(InstantiationException
              | IllegalAccessException
              | ClassNotFoundException e){
            String errorMsg = "Could not instantiate input program: ";
            getLog().error(errorMsg + program, e);
            throw new MojoExecutionException(errorMsg, e);
        }
    }

    private ClassRealm getClassRealm() throws MojoExecutionException {
        ClassRealm realm = descriptor.getClassRealm();
        addClasspathElementsToRealm(getClasspathElements(), realm);
        return realm;
    }

    @SuppressWarnings("unchecked")
    private List<String> getClasspathElements() throws MojoExecutionException {
        List<String> runtimeClasspathElements;
        try {
            runtimeClasspathElements = project.getRuntimeClasspathElements();
        } catch (Exception e) {
            throw new MojoExecutionException("Could not get dependencies", e);
        }
        return runtimeClasspathElements;
    }

    private void addClasspathElementsToRealm(List<String> runtimeClasspathElements, ClassRealm realm)
            throws MojoExecutionException {
        for (String element : runtimeClasspathElements) {
            File elementFile = new File(element);
            try {
                URL url = elementFile.toURI().toURL();
                getLog().info("Adding classpath element: " + url.toString());
                realm.addURL(url);
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Malformed URL for file " + elementFile.toString(), e);
            }
        }
    }

    private void populateProgramInputs(
            final CodegenProgram program,
            final ClassRealm realm) 
                    throws MojoExecutionException {
        try {
            populateProgramInputsUnchecked(program, realm);
        }
        catch(IllegalAccessException e) {
            String msg = "This cannot happen";
            getLog().error(msg, e);
            throw new MojoExecutionException(msg, e);
        }
    }

    private void populateProgramInputsUnchecked(
            final CodegenProgram program,
            final ClassRealm realm) 
                    throws IllegalAccessException, MojoExecutionException {
        for (Field inputField : getInputFields(program)) {
            Input annotation = inputField.getAnnotation(Input.class);
            String source = annotation.value();
            inputField.set(program, getClassModel(source, realm));
        }
    }

    private Set<Field> getInputFields(final CodegenProgram program) {
        @SuppressWarnings("unchecked")
        Set<Field> inputFields = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withTypeAssignableTo(ClassModel.class),
                ReflectionUtils.withAnnotation(Input.class));
        return inputFields;
    }


    private ClassModel getClassModel(final String source, final ClassRealm realm) 
            throws MojoExecutionException {
        ClassModel result = new ClassModel();
        result.setFullName(source);
        try {
            result.setMethods(getMethods(source, realm));
        }
        catch(ClassNotFoundException e) {
            String msg = String.format("Program %s references class that is not available: %s",
                    program, source);
            getLog().error(msg);
            throw new MojoExecutionException(msg, e);
        }
        return result;
    }

    private List<MethodModel> getMethods(final String fullClassName, final ClassRealm realm)
            throws ClassNotFoundException {
        Class<?> clazz = realm.loadClass(fullClassName);
        Method[] reflectionMethods = clazz.getMethods();
        List<MethodModel> result = new ArrayList<>();
        for (Method reflectionMethod : reflectionMethods) {
            result.add(new MethodModel(reflectionMethod));
        }
        return result;
    }

    private void populateOutputFields(final CodegenProgram program)
            throws MojoExecutionException {
        try {
            populateOutputFieldsUnchecked(program);
        }
        catch(IllegalAccessException e) {
            String msg = "Could not initialize output field with velocity context";
            getLog().error(msg, e);
            throw new MojoExecutionException(msg, e);
        }
    }

    private void populateOutputFieldsUnchecked(final CodegenProgram program) 
            throws MojoExecutionException, IllegalAccessException {
        for(Field outputField : getOutputFields(program)) {
            outputField.set(program, new VelocityContext());
        }
    }

    private Set<Field> getOutputFields(final CodegenProgram program) {
        @SuppressWarnings("unchecked")
        Set<Field> outputFields = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withTypeAssignableTo(VelocityContext.class),
                ReflectionUtils.withAnnotation(Output.class));
        return outputFields;
    }

    private void createOutputFiles(final CodegenProgram program) throws MojoExecutionException {
        try {
            createOutputFilesUnchecked(program);
        } catch (IllegalAccessException e) {
            String msg = "Output is not a velocity context";
            getLog().error(msg, e);
            throw new MojoExecutionException(msg, e);
        }
    }

    private void createOutputFilesUnchecked(final CodegenProgram program)
            throws MojoExecutionException, IllegalAccessException {
        for (Field outputField : getOutputFields(program)) {
            Output annotation = outputField.getAnnotation(Output.class);
            String template = annotation.value();
            VelocityContext velocityContext = (VelocityContext) outputField.get(program);
            ClassModel outputClassModel = (ClassModel) velocityContext.get("target");
            String outputFile = outputClassModel.getFullName();
            writeOutputFile(velocityContext, template, outputFile);
        }
    }

    private void writeOutputFile(
            VelocityContext velocityContext,
            String templateFileName,
            String outputClass) throws MojoExecutionException {
        Writer writer = null;
        File fileToWrite = classToPathOfJavaFile(outputDirectory, outputClass);
        try {
            writer = writeOutputFileUnchecked(velocityContext, templateFileName, outputClass, fileToWrite);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write file " + fileToWrite.toString(), e);
        } finally {
            checkedClose(writer);
        }
    }

    static File classToPathOfJavaFile(File base, String className) {
        String[] components = className.split("\\.");
        File result = new File(base, javaNameToPathComponent(components, 0));
        if (components.length >= 2) {
            for (int i = 1; i < components.length; ++i) {
                result = new File(result, javaNameToPathComponent(components, i));
            }
        }
        return result;
    }

    private static String javaNameToPathComponent(final String[] components, final int index) {
        if (index < (components.length - 1)) {
            return components[index];
        } else {
            return components[index] + ".java";
        }
    }

    private Writer writeOutputFileUnchecked(
            VelocityContext velocityContext,
            String templateFileName,
            String outputClass,
            File fileToWrite) throws IOException {
        getLog().info(String.format("Applying template %s to create class %s in file %s",
                templateFileName, outputClass, fileToWrite.toString()));
        fileToWrite.getParentFile().mkdirs();
        Template template = velocityComponent.getEngine().getTemplate(
                templateFileName);        
        Writer writer = new OutputStreamWriter(
                buildContext.newFileOutputStream(fileToWrite), sourceEncoding);
        template.merge(velocityContext, writer);
        return writer;
    }

    private void checkedClose(Writer writer) throws MojoExecutionException {
        try {
            writer.close();
        } catch(IOException e) {
            throw new MojoExecutionException("Could not close file" , e);
        }
    }
}
