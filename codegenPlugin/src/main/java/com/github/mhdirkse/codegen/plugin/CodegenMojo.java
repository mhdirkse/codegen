package com.github.mhdirkse.codegen.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import com.github.mhdirkse.codegen.plugin.lang.CodegenLexer;
import com.github.mhdirkse.codegen.plugin.lang.CodegenParser;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;
import com.github.mhdirkse.codegen.plugin.model.MethodModel;
import com.github.mhdirkse.codegen.plugin.model.VelocityEntry;
import com.github.mhdirkse.codegen.plugin.model.VelocityTask;

/**
 * Goal which generates .java files from POJO description files.
 */
@Mojo(name = "codegen", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class CodegenMojo extends AbstractMojo {
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

    @Parameter(defaultValue = "${project.build.resources[0].directory}/Codegen")
    private File codegenProgram;

    @Parameter
    List<Task> tasks;

    public void execute() throws MojoExecutionException {
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath().toString());
        ClassRealm realm = getClassRealm();
        for (VelocityTask task : parseProgram(realm)) {
            createOutputFile(task);
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

    private List<VelocityTask> parseProgram(final ClassRealm realm) throws MojoExecutionException {
        Reader programReader = getProgramReader();
        try {
            return parseProgramImpl(programReader, new CodegenListenerHelperImpl(realm));
        }
        catch(ParseCancellationException e) {
            getLog().error(e);
            throw new MojoExecutionException("Could not parse program", e);
        }
    }

    private Reader getProgramReader() throws MojoExecutionException {
        try {
            InputStream in = new FileInputStream(codegenProgram);
            return new BufferedReader(
                    new InputStreamReader(in, sourceEncoding));
        }
        catch(final IOException e) {
            throw new MojoExecutionException("Codegen program file could not be opened: " + codegenProgram, e);
        }
    }

    private List<VelocityTask> parseProgramImpl(final Reader programReader, CodegenListenerHelper helper)
    throws MojoExecutionException
    {
        CodegenLexer lexer = null;
        try {
            lexer = new CodegenLexer(new ANTLRInputStream(programReader));
        }
        catch(final IOException e) {
            throw new MojoExecutionException("IO error while reading program file " + codegenProgram, e);
        }
        lexer.removeErrorListeners();
        ANTLRErrorListener errorListener = new ErrorListenerImpl();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CodegenParser parser = new CodegenParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        ParseTreeWalker walker = new ParseTreeWalker();
        CodegenListener listener = new CodegenListener(helper);
        walker.walk(listener, parser.prog());
        return listener.getTasks();
    }

    private class ErrorListenerImpl extends ConsoleErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                String msg, RecognitionException e) {
            getLog().error(Utils.getErrorMessage(line, charPositionInLine, msg));
        }
    }

    private class CodegenListenerHelperImpl implements CodegenListenerHelper {
        private final ClassRealm realm;

        private CodegenListenerHelperImpl(final ClassRealm realm) {
            this.realm = realm;
        }

        @Override
        public List<MethodModel> getMethods(final String fullClassName) throws ClassNotFoundException {
            Class<?> clazz = realm.loadClass(fullClassName);
            Method[] reflectionMethods = clazz.getMethods();
            List<MethodModel> result = new ArrayList<>();
            for (Method reflectionMethod : reflectionMethods) {
                result.add(new MethodModel(reflectionMethod));
            }
            return result;
        }
    }

    void createOutputFile(final VelocityTask task) {
        VelocityContext ctx = new VelocityContext();
        for (VelocityEntry entry : task.getVelocityEntries()) {
            ctx.put(entry.getEntryName(), entry.getClassModel());
        }
        writeOutputFile(
                ctx,
                getTemplatePath(task.getTemplateName()),
                task.getOutputClassName());
    }

    private void handleTask(Task task, ClassRealm realm) throws MojoExecutionException {
        Method[] reflectionMethods = processJavaInterface(task.getSource(), realm);
        writeOutputFile(
                getVelocityContextForJavaHandlerInterface(task, reflectionMethods),
                getTemplatePath("handlerInterfaceTemplate"),
                task.getHandler());
        writeOutputFile(
                getVelocityContextForJavaAbstractHandler(task, reflectionMethods),
                getTemplatePath("abstractHandlerClassTemplate"),
                task.getAbstractHandler());
        writeOutputFile(
                getVelocityContextForDelegator(task, reflectionMethods),
                getTemplatePath("delegatorClassTemplate"),
                task.getDelegator());
    }

    private Method[] processJavaInterface(String interfaceToProcess, ClassRealm realm) throws MojoExecutionException {
        Class<?> myInterfaceClazz;
        try {
            myInterfaceClazz = realm.loadClass(interfaceToProcess);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Class not found: " + interfaceToProcess);
        }
        return myInterfaceClazz.getMethods();
    }

    private VelocityContext getVelocityContextForJavaHandlerInterface(final Task task, final Method[] reflectionMethods) {
        VelocityContext result = new VelocityContext();
        ClassModel source = getSourceClassModel(task, reflectionMethods);
        ClassModel target = getJavaHandlerClassModel(task, source);        
        result.put("target", target);
        return result;
    }

    private ClassModel getSourceClassModel(final Task task, final Method[] reflectionMethods) {
        ClassModel source = new ClassModel();
        source.setFullName(task.getSource());
        source.setMethods(reflectionMethods);
        return source;
    }

    private ClassModel getJavaHandlerClassModel(final Task task, ClassModel source) {
        ClassModel target = new ClassModel(source);
        target.setFullName(task.getHandler());
        target.setReturnTypeForAllMethods("boolean");
        target.addParameterTypeToAllMethods(
                makeType("com.github.mhdirkse.codegen.runtime.HandlerStackContext", task.getHandler()));
        return target;
    }

    private String makeType(final String base, final String typeParameter) {
        return base + "<" + typeParameter + ">";
    }

    private VelocityContext getVelocityContextForJavaAbstractHandler(final Task task, final Method[] reflectionMethods) {
        VelocityContext result = new VelocityContext();
        ClassModel source = getJavaHandlerClassModel(task, getSourceClassModel(task, reflectionMethods));
        ClassModel target = getJavaAbstractHandlerClassModel(task, source);
        result.put("source", source);
        result.put("target", target);
        return result;
    }

    private ClassModel getJavaAbstractHandlerClassModel(final Task task, ClassModel source) {
        ClassModel target = new ClassModel(source);
        target.setFullName(task.getAbstractHandler());
        return target;
    }

    private VelocityContext getVelocityContextForDelegator(final Task task, final Method[] reflectionMethods) {
        VelocityContext result = new VelocityContext();
        ClassModel source = getSourceClassModel(task, reflectionMethods);
        ClassModel handler = getJavaHandlerClassModel(task, source);
        ClassModel target = getJavaDelegatorClassModel(task, source);
        result.put("source", source);
        result.put("handler", handler);
        result.put("target", target);
        return result;
    }

    private ClassModel getJavaDelegatorClassModel(final Task task, final ClassModel source) {
        ClassModel target = new ClassModel(source);
        target.setFullName(task.getDelegator());
        return target;
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
        fileToWrite.getParentFile().mkdirs();
        Template template = velocityComponent.getEngine().getTemplate(
                templateFileName);        
        Writer writer = new OutputStreamWriter(
                buildContext.newFileOutputStream(fileToWrite));
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
