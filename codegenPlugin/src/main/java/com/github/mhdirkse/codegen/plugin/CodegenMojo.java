package com.github.mhdirkse.codegen.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.MethodModel;
import com.github.mhdirkse.codegen.compiletime.Output;

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
        Runnable instantiatedProgram = instantiate(program, Runnable.class);
        ClassRealm realm = getClassRealm();
        populateProgramInputs(instantiatedProgram, realm);
        populateOutputFields(instantiatedProgram);
        instantiatedProgram.run();
        createOutputFiles(instantiatedProgram);
    }

    private <T> T instantiate(final String className, final Class<T> type) throws MojoExecutionException {
        try{
            ClassLoader mavenPluginClassLoader = this.getClass().getClassLoader();
            return type.cast(mavenPluginClassLoader.loadClass(className).newInstance());
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
            final Runnable program,
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
            final Runnable program,
            final ClassRealm realm) 
                    throws IllegalAccessException, MojoExecutionException {
        for (Field inputField : getInputFields(program, this)) {
            Input annotation = inputField.getAnnotation(Input.class);
            String source = annotation.value();
            inputField.set(program, getClassModel(source, realm));
        }
    }

    static Set<Field> getInputFields(final Runnable program, final Logger logger)
            throws MojoExecutionException {
        InputFieldsAnalyzer a = new InputFieldsAnalyzer(program, logger);
        a.run();
        return a.inputFields;
    }

    private static class InputFieldsAnalyzer {
        private final Runnable program;
        private final Logger logger;

        Set<Field> inputFields;
        Set<Field> inputFieldsAnyModifier;
        Set<Field> inputFieldsAnyType;
        Set<Field> inputFieldsAny;
        
        InputFieldsAnalyzer(
                final Runnable program,
                final Logger logger) {
            this.program = program;
            this.logger = logger;
        }

        void run() throws MojoExecutionException {
            getInputFields();
            getInputFieldsAnyModifier();
            getInputFieldsAnyType();
            getInputFieldsAllWrong();
            checkAllInputFieldsPublic();
            checkAllInputFieldsClassModel();
            checkNoInputFieldsAllWrong();
        }

        @SuppressWarnings("unchecked")
        private void getInputFields() {
            inputFields = ReflectionUtils.getAllFields(
                    program.getClass(),
                    ReflectionUtils.withTypeAssignableTo(ClassModel.class),
                    ReflectionUtils.withAnnotation(Input.class),
                    ReflectionUtils.withModifier(Modifier.PUBLIC));
        }

        @SuppressWarnings("unchecked")
        private void getInputFieldsAnyModifier() {
            inputFieldsAnyModifier = ReflectionUtils.getAllFields(
                    program.getClass(),
                    ReflectionUtils.withTypeAssignableTo(ClassModel.class),
                    ReflectionUtils.withAnnotation(Input.class));
        }

        @SuppressWarnings("unchecked")
        private void getInputFieldsAnyType() {
            inputFieldsAnyType = ReflectionUtils.getAllFields(
                    program.getClass(),
                    ReflectionUtils.withAnnotation(Input.class),
                    ReflectionUtils.withModifier(Modifier.PUBLIC));
        }

        @SuppressWarnings("unchecked")
        private void getInputFieldsAllWrong() {
            inputFieldsAny = ReflectionUtils.getAllFields(
                    program.getClass(),
                    ReflectionUtils.withAnnotation(Input.class));
        }

        private void checkAllInputFieldsPublic() throws MojoExecutionException {
            Set<Field> nonPublicInputFields = new HashSet<>(inputFieldsAnyModifier);
            nonPublicInputFields.removeAll(inputFields);
            if(!nonPublicInputFields.isEmpty()) {
                String msg = String.format("Some input fields are not public: %s", 
                        StringUtils.join(getFieldNames(nonPublicInputFields), ", "));
                logger.error(msg);
                throw new MojoExecutionException(msg);
            }
        }

        private void checkAllInputFieldsClassModel() throws MojoExecutionException {
            Set<Field> nonClassModelInputFields = new HashSet<>(inputFieldsAnyType);
            nonClassModelInputFields.removeAll(inputFields);
            if(!nonClassModelInputFields.isEmpty()) {
                String msg = String.format("Some input fields are not ClassModel: %s", 
                        StringUtils.join(getFieldNames(nonClassModelInputFields), ", "));
                logger.error(msg);
                throw new MojoExecutionException(msg);
            }
        }

        private void checkNoInputFieldsAllWrong() throws MojoExecutionException {
            Set<Field> allWrongInputFields = new HashSet<>(inputFieldsAny);
            allWrongInputFields.removeAll(inputFields);
            if(!allWrongInputFields.isEmpty()) {
                String msg = String.format("Some input fields are not ClassModel and not public: %s", 
                        StringUtils.join(getFieldNames(allWrongInputFields), ", "));
                logger.error(msg);
                throw new MojoExecutionException(msg);
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

    private void populateOutputFields(final Runnable program)
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

    private void populateOutputFieldsUnchecked(final Runnable program) 
            throws MojoExecutionException, IllegalAccessException {
        for(Field outputField : getOutputFields(program, this)) {
            outputField.set(program, new VelocityContext());
        }
    }

    static Set<Field> getOutputFields(final Runnable program, Logger logger) 
            throws MojoExecutionException {
        OutputFieldsAnalyzer a = new OutputFieldsAnalyzer(program, logger);
        a.run();
        return a.outputFields;
    }

    private static class OutputFieldsAnalyzer {
        final Runnable program;
        final Logger logger;
        Set<Field> outputFields;
        Set<Field> outputFieldsAnyModifier;
        Set<Field> outputFieldsAnyType;
        Set<Field> outputFieldsAny;

        OutputFieldsAnalyzer(
                final Runnable program,
                final Logger logger) {
            this.program = program;
            this.logger = logger;
        }

        void run() throws MojoExecutionException {
            getOutputFields();
            getOutputFieldsAnyModifier();
            getOutputFieldsAnyType();
            getOutputFieldsAny();
            checkAllPublic();
            checkAllVelocityContext();
            checkNoneAllWrong();
        }

        @SuppressWarnings("unchecked")
        private void getOutputFields() {
            outputFields = ReflectionUtils.getAllFields(
                    program.getClass(),
                    ReflectionUtils.withTypeAssignableTo(VelocityContext.class),
                    ReflectionUtils.withAnnotation(Output.class),
                    ReflectionUtils.withModifier(Modifier.PUBLIC));
        }

        @SuppressWarnings("unchecked")
        private void getOutputFieldsAnyModifier() {
            outputFieldsAnyModifier = ReflectionUtils.getAllFields(
                    program.getClass(),
                    ReflectionUtils.withTypeAssignableTo(VelocityContext.class),
                    ReflectionUtils.withAnnotation(Output.class));
        }

        @SuppressWarnings("unchecked")
        private void getOutputFieldsAnyType() {
            outputFieldsAnyType = ReflectionUtils.getAllFields(
                    program.getClass(),
                    ReflectionUtils.withAnnotation(Output.class),
                    ReflectionUtils.withModifier(Modifier.PUBLIC));
        }

        @SuppressWarnings("unchecked")
        private void getOutputFieldsAny() {
            outputFieldsAny = ReflectionUtils.getAllFields(
                    program.getClass(),
                    ReflectionUtils.withAnnotation(Output.class));
        }

        private void checkAllPublic() throws MojoExecutionException {
            Set<Field> nonPublicFields = new HashSet<>(outputFieldsAnyModifier);
            nonPublicFields.removeAll(outputFields);
            if (!nonPublicFields.isEmpty()) {
                String msg = String.format("Output fields %s are not public",
                        StringUtils.join(getFieldNames(nonPublicFields), ", "));
                logger.error(msg);
                throw new MojoExecutionException(msg);
            }
        }

        private void checkAllVelocityContext() throws MojoExecutionException {
            Set<Field> nonVelocityContextFields = new HashSet<>(outputFieldsAnyType);
            nonVelocityContextFields.removeAll(outputFields);
            if (!nonVelocityContextFields.isEmpty()) {
                String msg = String.format("Output fields %s are not VelocityContext",
                        StringUtils.join(getFieldNames(nonVelocityContextFields), ", "));
                logger.error(msg);
                throw new MojoExecutionException(msg);
            }
        }

        private void checkNoneAllWrong() throws MojoExecutionException {
            Set<Field> allWrongFields = new HashSet<>(outputFieldsAny);
            allWrongFields.removeAll(outputFields);
            if (!allWrongFields.isEmpty()) {
                String msg = String.format("Output fields %s are not VelocityContext and not public",
                        StringUtils.join(getFieldNames(allWrongFields), ", "));
                logger.error(msg);
                throw new MojoExecutionException(msg);
            }
        }        
    }

    private static List<String> getFieldNames(Collection<Field> fields) {
        List<String> result = new ArrayList<>();
        for (Field field : fields) {
            result.add(field.getName());
        }
        return result;
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
        for (Field outputField : getOutputFields(program, this)) {
            Output annotation = outputField.getAnnotation(Output.class);
            String template = annotation.value();
            VelocityContext velocityContext = (VelocityContext) outputField.get(program);
            ClassModel outputClassModel = getTarget(velocityContext, outputField.getName(), this);
            String outputFile = outputClassModel.getFullName();
            writeOutputFile(velocityContext, template, outputFile);
        }
    }

    static ClassModel getTarget(
            final VelocityContext velocityContext,
            final String fieldName,
            final Logger logger) 
                throws MojoExecutionException {
        if (!velocityContext.containsKey("target")) {
            String msg = String.format("Cannot get output file from VelocityContext %s", 
                    fieldName);
            logger.error(msg);
            throw new MojoExecutionException(msg);
        }
        Object targetAsObject = velocityContext.get("target");
        if (!(targetAsObject instanceof ClassModel)) {
            String msg = String.format("Expected ClassModel as target in VelocityContext %s",
                    fieldName);
            logger.error(msg);
            throw new MojoExecutionException(msg);
        }
        return (ClassModel) targetAsObject;
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
