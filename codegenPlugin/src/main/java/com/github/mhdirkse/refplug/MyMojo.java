package com.github.mhdirkse.refplug;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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

/**
 * Goal which generates .java files from POJO description files.
 */
@Mojo(name = "reflection", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class MyMojo extends AbstractMojo {
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

    @Parameter
    List<Task> tasks;

    public void execute() throws MojoExecutionException {
        ClassRealm realm = getClassRealm();
        for (Task task : tasks) {
            processInterface(task.getSource(), realm);
            Writer writer = null;
            File fileToWrite = classToPath(outputDirectory, task.getHandler());
            try {
                fileToWrite.getParentFile().mkdirs();
                Template template = velocityComponent.getEngine().getTemplate("simpleVelocityTemplate");
                writer = new OutputStreamWriter(
                        buildContext.newFileOutputStream(fileToWrite));
                template.merge(new VelocityContext(), writer);
            } catch (IOException e) {
                throw new MojoExecutionException("Could not write file " + fileToWrite, e);
            } finally {
                checkedClose(writer);
            }
        }
    }

    private void checkedClose(Writer writer) throws MojoExecutionException {
        try {
            writer.close();
        } catch(IOException e) {
            throw new MojoExecutionException("Could not close file" , e);
        }
    }

    private ClassRealm getClassRealm() throws MojoExecutionException {
        List<String> runtimeClasspathElements = getClasspathElements();
        ClassRealm realm = descriptor.getClassRealm();

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

    private void processInterface(String interfaceToProcess, ClassRealm realm) throws MojoExecutionException {
        Class<?> myInterfaceClazz;
        try {
            myInterfaceClazz = realm.loadClass(interfaceToProcess);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Class not found: " + interfaceToProcess);
        }

        Method[] methods = myInterfaceClazz.getMethods();
        for (Method method : methods) {
            getLog().info("Found method: " + method.getName());
        }
    }

    static File classToPath(File base, String className) {
        String[] components = className.split("\\.");
        File result = new File(base, components[0]);
        if (components.length >= 2) {
            for (int i = 1; i < components.length; ++i) {
                result = new File(result, components[i]);
            }
        }
        return result;
    }
}
