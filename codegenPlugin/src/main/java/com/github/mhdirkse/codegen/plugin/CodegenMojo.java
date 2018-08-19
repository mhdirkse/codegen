package com.github.mhdirkse.codegen.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

import com.github.mhdirkse.codegen.compiletime.ClassModel;
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
    public void debug(final String msg) {
        getLog().debug(msg);
    }

    @Override
    public void info(final String msg) {
        getLog().info(msg);
    }

    @Override
    public void error(final String msg) {
        getLog().error(msg);
    }

    @Override
    public void debug(final String msg, final Throwable e) {
        getLog().debug(msg, e);
    }

    @Override
    public void info(final String msg, final Throwable e) {
        getLog().info(msg, e);
    }

    @Override
    public void error(final String msg, final Throwable e) {
        getLog().error(msg, e);
    }

    Testable testable = new TestableImpl();

    @Override
    public void execute() throws MojoExecutionException {
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath().toString());
        Runnable instantiatedProgram = instantiate(program, Runnable.class);
        ClassLoaderAdapter cla = ClassLoaderAdapter.forRealm(Object.class, getClassRealm());
        List<Optional<FieldManipulation>> outputManipulations = new FieldIterator.OutputPopulator(instantiatedProgram, this).run();
        List<Optional<FieldManipulation>> rawManipulations = new ArrayList<>();
        rawManipulations.addAll(new FieldIterator.InputPopulator(instantiatedProgram, this, cla).run());
        rawManipulations.addAll(outputManipulations);
        rawManipulations.addAll(new FieldIterator.HierarchyPopulator(instantiatedProgram, this, cla).run());
        List<FieldManipulation> manipulations = rawManipulations.stream()
                .filter(Optional::isPresent)
                .map((om) -> om.get())
                .collect(Collectors.toList());
        boolean manipulationsOk = (manipulations.size() == rawManipulations.size());
        if (!manipulationsOk) {
            String msg = "Some field manipulations could not be created. Please see Maven console output.";
            error(msg);
            throw new MojoExecutionException(msg);
        }
        boolean populated = (runAllManipulations(manipulations));
        if(!populated) {
            String msg = "Could not populate the provided program. Please see Maven console output.";
            error(msg);
            throw new MojoExecutionException(msg);
        } else {
            instantiatedProgram.run();
            createOutputFiles(outputManipulations);
        }
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
                getLog().debug("Adding classpath element: " + url.toString());
                realm.addURL(url);
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Malformed URL for file " + elementFile.toString(), e);
            }
        }
    }

    boolean runAllManipulations(final List<FieldManipulation> manipulations) {
        return manipulations.stream()
            .map(FieldManipulation::run)
            .filter(CodegenMojo::failed)
            .collect(Collectors.counting()) == 0;
    }

    static boolean failed(final boolean manipulationResult) {
        return !manipulationResult;
    }

    private void createOutputFiles(
            final List<Optional<FieldManipulation>> outputManipulations) throws MojoExecutionException {
        for(Optional<FieldManipulation> fm : outputManipulations) {
            createOutputFileUnchecked(fm.get());    
        }
    }

    private void createOutputFileUnchecked(final FieldManipulation fm) throws MojoExecutionException {
        Output annotation = fm.f.getAnnotation(Output.class);
        String template = annotation.value();
        VelocityContext velocityContext = (VelocityContext) fm.instantiatedObject;
        ClassModel outputClassModel = testable.getTarget(velocityContext, fm.f.getName());
        String outputFile = outputClassModel.getFullName();
        writeOutputFile(velocityContext, template, outputFile);
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
        info(String.format("Applying template %s to create class %s in file %s", templateFileName, outputClass,
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

        ClassModel getTarget(final VelocityContext velocityContext, final String fieldName)
                throws MojoExecutionException {
            if (!velocityContext.containsKey("target")) {
                String msg = String.format("VelocityContext %s does not have \"target\" member. Cannot get output file name.", fieldName);
                getLogger().error(msg);
                throw new MojoExecutionException(msg);
            }
            Object targetAsObject = velocityContext.get("target");
            if (!(targetAsObject instanceof ClassModel)) {
                String msg = String.format("Expected a ClassModel as \"target\" in VelocityContext %s. Cannot get output file name.", fieldName);
                getLogger().error(msg);
                throw new MojoExecutionException(msg);
            }
            ClassModel targetAsClassModel = (ClassModel) targetAsObject;
            if(StringUtils.isBlank(targetAsClassModel.getFullName())) {
                String msg = String.format("ClassModel \"target\" does not have fullName, for field %s.",
                        fieldName);
                getLogger().error(msg);
                throw new MojoExecutionException(msg);
            }
            return targetAsClassModel;
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
