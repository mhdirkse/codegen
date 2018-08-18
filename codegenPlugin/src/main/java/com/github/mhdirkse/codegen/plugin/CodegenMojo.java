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
import org.reflections.Reflections;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.MethodModel;
import com.github.mhdirkse.codegen.compiletime.Output;
import com.github.mhdirkse.codegen.compiletime.TypeHierarchy;

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

    Testable testable = new TestableImpl();

    @Override
    public void execute() throws MojoExecutionException {
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath().toString());
        Runnable instantiatedProgram = instantiate(program, Runnable.class);
        ClassRealm realm = getClassRealm();
        MojoExecutionExceptionAdapter.list().add("Populate program inputs", () -> {
            this.populateProgramInputsUnchecked(instantiatedProgram, realm);
        }).add("Populate type hierarchies", () -> {
            this.populateTypeHierarchiesUnchecked(instantiatedProgram, realm);
        }).add("Populate output fields", () -> {
            this.populateOutputFieldsUnchecked(instantiatedProgram);
        }).run();
        instantiatedProgram.run();
        createOutputFiles(instantiatedProgram);
    }

    private <T> T instantiate(final String className, final Class<T> type) throws MojoExecutionException {
        try {
            ClassLoader mavenPluginClassLoader = this.getClass().getClassLoader();
            return type.cast(mavenPluginClassLoader.loadClass(className).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
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

    private void populateProgramInputsUnchecked(final Runnable program, final ClassRealm realm)
            throws IllegalAccessException, MojoExecutionException {
        for (Field inputField : testable.getInputFields(program)) {
            Input annotation = inputField.getAnnotation(Input.class);
            String source = annotation.value();
            inputField.set(program,
                    testable.getClassModel(source, ClassLoaderAdapter.forRealm(Object.class, realm)));
        }
    }

    private void populateTypeHierarchiesUnchecked(final Runnable program, final ClassRealm realm)
            throws MojoExecutionException, IllegalAccessException {
        for (Field field : testable.getTypeHierarchyFields(program)) {
            TypeHierarchy annotation = field.getAnnotation(TypeHierarchy.class);
            String root = annotation.value();
            Class<?> rootClass = testable.getClass(root, ClassLoaderAdapter.forRealm(Object.class, realm));
            ClassLoaderAdapter cla = ClassLoaderAdapter.forRealm(rootClass, realm);
            field.set(program, testable.getHierarchy(rootClass, cla));
        }
    }

    private void populateOutputFieldsUnchecked(final Runnable program)
            throws MojoExecutionException, IllegalAccessException {
        for (Field outputField : testable.getOutputFields(program)) {
            outputField.set(program, new VelocityContext());
        }
    }

    private void createOutputFiles(final Runnable program) throws MojoExecutionException {
        try {
            createOutputFilesUnchecked(program);
        } catch (IllegalAccessException e) {
            String msg = "This cannot happen";
            getLog().error(msg, e);
            throw new MojoExecutionException(msg, e);
        }
    }

    private void createOutputFilesUnchecked(final Runnable program)
            throws MojoExecutionException, IllegalAccessException {
        for (Field outputField : testable.getOutputFields(program)) {
            Output annotation = outputField.getAnnotation(Output.class);
            String template = annotation.value();
            VelocityContext velocityContext = (VelocityContext) outputField.get(program);
            ClassModel outputClassModel = testable.getTarget(velocityContext, outputField.getName());
            String outputFile = outputClassModel.getFullName();
            writeOutputFile(velocityContext, template, outputFile);
        }
    }

    private void writeOutputFile(VelocityContext velocityContext, String templateFileName, String outputClass)
            throws MojoExecutionException {
        Writer writer = null;
        File fileToWrite = Testable.classToPathOfJavaFile(outputDirectory, outputClass);
        try {
            writer = writeOutputFileUnchecked(velocityContext, templateFileName, outputClass, fileToWrite);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write file " + fileToWrite.toString(), e);
        } finally {
            checkedClose(writer);
        }
    }

    private Writer writeOutputFileUnchecked(VelocityContext velocityContext, String templateFileName,
            String outputClass, File fileToWrite) throws IOException {
        getLog().info(String.format("Applying template %s to create class %s in file %s", templateFileName, outputClass,
                fileToWrite.toString()));
        fileToWrite.getParentFile().mkdirs();
        Template template = velocityComponent.getEngine().getTemplate(templateFileName);
        Writer writer = new OutputStreamWriter(buildContext.newFileOutputStream(fileToWrite), sourceEncoding);
        template.merge(velocityContext, writer);
        return writer;
    }

    private void checkedClose(Writer writer) throws MojoExecutionException {
        try {
            writer.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Could not close file", e);
        }
    }

    private class TestableImpl extends Testable {
        @Override
        Logger getLogger() {
            return CodegenMojo.this;
        }

        @Override
        String getProgram() {
            return CodegenMojo.this.program;
        }
    }

    static abstract class Testable {
        abstract Logger getLogger();
        abstract String getProgram();

        Set<Field> getInputFields(final Runnable program) throws MojoExecutionException {
            FieldsAnalyzer a = new FieldsAnalyzer(program, getLogger());
            a.targetAnnotation = Input.class;
            a.targetType = ClassModel.class;
            a.run();
            return a.fields;
        }

        ClassModel getClassModel(final String source, final ClassLoaderAdapter cla) throws MojoExecutionException {
            ClassModel result = new ClassModel();
            result.setFullName(source);
            result.setMethods(getMethods(getClass(source, cla)));
            return result;
        }

        Class<?> getClass(final String source, final ClassLoaderAdapter cla) throws MojoExecutionException {
            try {
                return cla.loadClass(source);
            } catch (ClassNotFoundException e) {
                String msg = String.format("Program %s references class that is not available: %s", getProgram(), source);
                getLogger().error(msg);
                throw new MojoExecutionException(msg, e);
            }
        }

        List<MethodModel> getMethods(final Class<?> clazz) {
            Method[] reflectionMethods = clazz.getMethods();
            List<MethodModel> result = new ArrayList<>();
            for (Method reflectionMethod : reflectionMethods) {
                result.add(new MethodModel(reflectionMethod));
            }
            return result;
        }

        Set<Field> getTypeHierarchyFields(final Runnable program) throws MojoExecutionException {
            FieldsAnalyzer a = new FieldsAnalyzer(program, getLogger());
            a.targetAnnotation = TypeHierarchy.class;
            a.targetType = ClassModelList.class;
            a.run();
            return a.fields;
        }

        <R> ClassModelList getHierarchy(final Class<R> root, final ClassLoaderAdapter cla) throws MojoExecutionException {
            Reflections r = new Reflections(root.getPackage().getName());
            Set<Class<? extends R>> subClasses = r.getSubTypesOf(root);
            subClasses.add(root);
            ClassModelList result = new ClassModelList();
            for (Class<? extends R> subClass : subClasses) {
                result.add(getClassModel(subClass.getName(), cla));
            }
            return result;
        }

        Set<Field> getOutputFields(final Runnable program) throws MojoExecutionException {
            FieldsAnalyzer a = new FieldsAnalyzer(program, getLogger());
            a.targetAnnotation = Output.class;
            a.targetType = VelocityContext.class;
            a.run();
            return a.fields;
        }

        ClassModel getTarget(final VelocityContext velocityContext, final String fieldName)
                throws MojoExecutionException {
            if (!velocityContext.containsKey("target")) {
                String msg = String.format("Cannot get output file from VelocityContext %s", fieldName);
                getLogger().error(msg);
                throw new MojoExecutionException(msg);
            }
            Object targetAsObject = velocityContext.get("target");
            if (!(targetAsObject instanceof ClassModel)) {
                String msg = String.format("Expected ClassModel as target in VelocityContext %s", fieldName);
                getLogger().error(msg);
                throw new MojoExecutionException(msg);
            }
            return (ClassModel) targetAsObject;
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
    }
}
