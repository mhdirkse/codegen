package com.github.mhdirkse.codegen.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.sonatype.plexus.build.incremental.BuildContext;

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
        Map<String, ClassModel> variables = getInitialVariables(instantiatedProgram);
        instantiatedProgram.setLogger(this);
        instantiatedProgram.run(variables);
        createOutputFiles(instantiatedProgram.getGenerators(), variables);
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

    private Map<String, ClassModel> getInitialVariables(final CodegenProgram instantiatedProgram) throws MojoExecutionException {
        ClassRealm realm = getClassRealm();
        Map<String, ClassModel> variables = new HashMap<>();
        for (String source : instantiatedProgram.getSourceClasses()) {
            ClassModel variable = getClassModel(source, realm);
            variables.put(variable.getSimpleName(), variable);
        }
        return variables;
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

    private void createOutputFiles(
            List<VelocityGenerator> generators,
            Map<String, ClassModel> variables) throws MojoExecutionException {
        for (VelocityGenerator generator : generators) {
            createOutputFile(generator, variables);
        }
    }

    void createOutputFile(
            final VelocityGenerator generator,
            final Map<String, ClassModel> variables) throws MojoExecutionException {
        generator.run(variables);
        writeOutputFile(
                generator.getVelocityContext(),
                getTemplatePath(generator.getTemplateName()),
                generator.getOutputClass());
    }

    private String getTemplatePath(final String simpleName) {
        return "com/github/mhdirkse/codegen/plugin/" + simpleName;
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
