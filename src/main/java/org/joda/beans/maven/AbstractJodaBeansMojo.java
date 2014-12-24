/*
 *  Copyright 2013 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.beans.maven;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract Joda-Beans Mojo.
 */
public class AbstractJodaBeansMojo extends AbstractMojo {

    /**
     * Skips the mojo.
     * @parameter alias="skip" property="joda.beans.skip"
     */
    private boolean _skip;
    /**
     * @parameter default-value="${project.build.sourceDirectory}" property="joda.beans.source.dir"
     * @required
     * @readonly
     */
    private String sourceDir;
    /**
     * @parameter default-value="${project.build.testSourceDirectory}" property="joda.beans.test.source.dir"
     * @required
     * @readonly
     */
    private String testSourceDir;
    /**
     * @parameter alias="indent" property="joda.beans.indent"
     */
    private String indent;
    /**
     * @parameter alias="prefix" property="joda.beans.prefix"
     */
    private String prefix;
    /**
     * @parameter alias="verbose" property="joda.beans.verbose"
     */
    private Integer verbose;
    /**
     * The Maven project.
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject _project;

    //-----------------------------------------------------------------------
    /**
     * Gets the source directory.
     * 
     * @return the source directory, not null
     */
    protected String getSourceDir() {
        return (sourceDir == null ? "" : sourceDir.trim());
    }

    /**
     * Gets the test source directory.
     * 
     * @return the test source directory, not null
     */
    protected String getTestSourceDir() {
        return (testSourceDir == null ? "" : testSourceDir.trim());
    }

    //-----------------------------------------------------------------------
    /**
     * Executes the Joda-Beans generator.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (_skip) {
            return;
        }
        if (getSourceDir().length() == 0) {
            throw new MojoExecutionException("Source directory must be specified");
        }
        ClassLoader classLoader = obtainClassLoader();
        Class<?> toolClass = null;
        try {
            toolClass = classLoader.loadClass("org.joda.beans.gen.BeanCodeGen");
        } catch (Exception ex) {
            getLog().info("Skipping as joda-beans is not in the project compile classpath");
            return;
        }
        List<String> argsList = buildArgs();
        runTool(toolClass, argsList);
    }

    /**
     * Builds the arguments to the tool.
     * 
     * @return the arguments, not null
     */
    protected List<String> buildArgs() {
        List<String> argsList = new ArrayList<String>();
        argsList.add("-R");
        if (indent != null) {
            argsList.add("-indent=" + indent);
        }
        if (prefix != null) {
            argsList.add("-prefix=" + prefix);
        }
        if (verbose != null) {
            argsList.add("-verbose=" + verbose);
        }
        return argsList;
    }

    /**
     * Runs the tool.
     * 
     * @param toolClass  the tool class, not null
     * @param argsList  the argument flags, not null
     * @return the number of changes
     * @throws MojoExecutionException if an error occurs
     * @throws MojoFailureException if a failure occurs
     */
    protected int runTool(Class<?> toolClass, List<String> argsList) throws MojoExecutionException, MojoFailureException {
        // invoke main source
        argsList.add(getSourceDir());
        int count = invoke(toolClass, argsList);
        // optionally invoke test source
        if (getTestSourceDir().length() > 0) {
            argsList.set(argsList.size() - 1, getTestSourceDir());
            count += invoke(toolClass, argsList);
        }
        return count;
    }

    private int invoke(Class<?> toolClass, List<String> argsList) throws MojoExecutionException, MojoFailureException {
        Method createFromArgsMethod = findCreateFromArgsMethod(toolClass);
        Method processMethod = findProcessMethod(toolClass);
        Object beanCodeGen = createBuilder(argsList, createFromArgsMethod);
        return invokeBuilder(processMethod, beanCodeGen);
    }

    private Method findCreateFromArgsMethod(Class<?> toolClass) throws MojoExecutionException {
        Method createFromArgsMethod = null;
        try {
            createFromArgsMethod = toolClass.getMethod("createFromArgs", String[].class);
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to find method BeanCodeGen.createFromArgs()");
        }
        return createFromArgsMethod;
    }

    private Method findProcessMethod(Class<?> toolClass) throws MojoExecutionException {
        Method processMethod = null;
        try {
            processMethod = toolClass.getMethod("process");
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to find method BeanCodeGen.process()");
        }
        return processMethod;
    }

    private Object createBuilder(List<String> argsList, Method createFromArgsMethod) throws MojoExecutionException, MojoFailureException {
        String[] args = argsList.toArray(new String[argsList.size()]);
        try {
            return createFromArgsMethod.invoke(null, new Object[] { args });
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Error invoking BeanCodeGen.createFromArgs()");
        } catch (IllegalAccessException ex) {
            throw new MojoExecutionException("Error invoking BeanCodeGen.createFromArgs()");
        } catch (InvocationTargetException ex) {
            throw new MojoFailureException("Invalid Joda-Beans Mojo configuration: " + ex.getCause().getMessage(), ex.getCause());
        }
    }

    private int invokeBuilder(Method processMethod, Object beanCodeGen) throws MojoExecutionException, MojoFailureException {
        try {
            return (Integer) processMethod.invoke(beanCodeGen);
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Error invoking BeanCodeGen.process()");
        } catch (IllegalAccessException ex) {
            throw new MojoExecutionException("Error invoking BeanCodeGen.process()");
        } catch (InvocationTargetException ex) {
            throw new MojoFailureException("Error while running Joda-Beans tool: " + ex.getCause().getMessage(), ex.getCause());
        }
    }

    /**
     * Obtains the classloader from a set of file paths.
     * 
     * @return the classloader, not null
     */
    private URLClassLoader obtainClassLoader() throws MojoExecutionException {
        getLog().debug("Finding joda-beans in classpath");
        List<String> compileClasspath = obtainClasspath();
        Set<URL> classpathUrlSet = new HashSet<URL>();
        for (String classpathEntry : compileClasspath) {
            File f = new File(classpathEntry);
            if (f.exists() && f.getPath().contains("joda")) {
                try {
                    getLog().debug("Found classpath: " + f);
                    classpathUrlSet.add(f.toURI().toURL());
                } catch (MalformedURLException ex) {
                    throw new RuntimeException("Error interpreting classpath entry as URL: " + classpathEntry, ex);
                }
            }
        }
        URL[] classpathUrls = classpathUrlSet.toArray(new URL[classpathUrlSet.size()]);
        return new URLClassLoader(classpathUrls, AbstractJodaBeansMojo.class.getClassLoader());
    }

    /**
     * Obtains the resolved classpath of dependencies.
     * 
     * @return the classpath, not null
     * @throws MojoExecutionException
     */
    @SuppressWarnings("unchecked")
    private List<String> obtainClasspath() throws MojoExecutionException {
        try {
            return _project.getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException ex) {
            throw new MojoExecutionException("Error obtaining dependencies", ex);
        }
    }

}
