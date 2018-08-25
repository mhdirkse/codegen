package com.github.mhdirkse.codegen.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

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

import com.github.mhdirkse.codegen.plugin.impl.ClassService;
import com.github.mhdirkse.codegen.plugin.impl.CodegenMojoDelegate;
import com.github.mhdirkse.codegen.plugin.impl.FileContentsDefinition;
import com.github.mhdirkse.codegen.plugin.impl.FileWriteService;
import com.github.mhdirkse.codegen.plugin.impl.Logger;
import com.github.mhdirkse.codegen.plugin.impl.ServiceFactory;

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

    private Runnable instantiatedProgram;

    private ClassRealm classRealm;

    private ServiceFactory serviceFactory;

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

    @Override
    public void execute() throws MojoExecutionException {
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath().toString());
        instantiatedProgram = instantiate(program, Runnable.class);
        classRealm = getClassRealm();
        serviceFactory = new ServiceFactoryImpl();
        CodegenMojoDelegate runner = new CodegenMojoDelegate(instantiatedProgram, serviceFactory);
        runner.run();
        if(runner.hasErrors()) {
            throw new MojoExecutionException("CodegenPlugin did not run successfully. See Maven console output for details");
        }
    }

    private class ServiceFactoryImpl extends ServiceFactory {
        private final ClassService classService;
        private final FileWriteService fileWriteService;

        ServiceFactoryImpl() {
            super(instantiatedProgram, CodegenMojo.this);
            classService = getClassService(this);
            fileWriteService = getFileWriteService();
        }

        @Override
        protected ClassService classService() {
            return classService;
        }

        @Override
        protected FileWriteService fileWriteService() {
            return fileWriteService;
        }
    }

    private FileWriteService getFileWriteService() {
        return new FileWriteService() {
            @Override
            public void write(final FileContentsDefinition def, final Consumer<Exception> errorCallback) {
                writeOutputFile(
                        def.getVelocityContext(),
                        def.getTemplateFileName(),
                        def.getOutputClassName(),
                        errorCallback);
            }
        };
    }

    private ClassService getClassService(final ServiceFactory sf) {
        return new ClassService(sf) {
            @Override
            protected Class<?> doLoadClass(String fullName) throws ClassNotFoundException {
                return classRealm.loadClass(fullName);
            }
        };
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

    private void writeOutputFile(
            final VelocityContext velocityContext,
            final String templateFileName,
            final String outputClass,
            final Consumer<Exception> errorCallback) {
        File fileToWrite = classToPathOfJavaFile(outputDirectory, outputClass);
        info(String.format("Applying template %s to create class %s in file %s", templateFileName, outputClass,
                fileToWrite.toString()));
        try(Writer writer = getWriter(fileToWrite)) {
            writeOutputFileUnchecked(velocityContext, templateFileName, writer);
        } catch (Exception e) {
            errorCallback.accept(e);
        }
    }

    private Writer getWriter(File fileToWrite)
            throws UnsupportedEncodingException, IOException {
        fileToWrite.getParentFile().mkdirs();
        Writer writer = new OutputStreamWriter(buildContext.newFileOutputStream(fileToWrite), sourceEncoding);
        return writer;
    }

    private void writeOutputFileUnchecked(VelocityContext velocityContext, String templateFileName,
            final Writer writer) throws IOException {
        Template template = velocityComponent.getEngine().getTemplate(templateFileName);
        template.merge(velocityContext, writer);
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
