package com.github.mhdirkse.refplug;

import java.io.File;
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
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * Goal which generates .java files from POJO description files.
 */
@Mojo(
  name = "reflection",
  defaultPhase = LifecyclePhase.GENERATE_SOURCES,
  requiresDependencyResolution = ResolutionScope.COMPILE,
  requiresProject = true)
public class MyMojo extends AbstractMojo
{
    @Component
    private MavenProject project;

    @Component
    private PluginDescriptor descriptor;

    public void execute()
        throws MojoExecutionException
    {
        String interfaceToProcess = "com.github.mhdirkse.simpledep.MyInterface";
        ClassRealm realm = getClassRealm();
        processInterface(interfaceToProcess, realm);
    }

    private ClassRealm getClassRealm() throws MojoExecutionException {
        List<String> runtimeClasspathElements = getClasspathElements();
        ClassRealm realm = descriptor.getClassRealm();

        for (String element : runtimeClasspathElements)
        {
            File elementFile = new File(element);
            try {
                URL url = elementFile.toURI().toURL();
                getLog().info("Adding classpath element: " + url.toString());
                realm.addURL(url);
            } catch(MalformedURLException e) {
                throw new MojoExecutionException(
                        "Malformed URL for file " + elementFile.toString(), e);
            }
        }
        return realm;
    }

    @SuppressWarnings("unchecked")
    private List<String> getClasspathElements() throws MojoExecutionException {
        List<String> runtimeClasspathElements;
        try {
            runtimeClasspathElements = project.getRuntimeClasspathElements();
        } catch(Exception e) {
            throw new MojoExecutionException("Could not get dependencies", e);
        }
        return runtimeClasspathElements;
    }

    private void processInterface(String interfaceToProcess, ClassRealm realm) throws MojoExecutionException {
        Class<?> myInterfaceClazz;
        try {
            myInterfaceClazz = realm.loadClass(interfaceToProcess);
        } catch(ClassNotFoundException e) {
            throw new MojoExecutionException("Class not found: " + interfaceToProcess);
        }

        Method[] methods = myInterfaceClazz.getMethods();
        for (Method method: methods) {
            getLog().info("Found method: " + method.getName());
        }
    }
}
