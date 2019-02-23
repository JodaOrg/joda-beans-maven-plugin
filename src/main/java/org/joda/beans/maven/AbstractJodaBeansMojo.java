/*
 *  Copyright 2013-present, Stephen Colebourne
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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Abstract Joda-Beans Mojo.
 */
public class AbstractJodaBeansMojo extends AbstractMojo {

    /**
     * Key for clearing messages.
     */
    private static final String JODA_BEANS_MESSAGE_FILE = "joda-beans.message.file";
    /**
     * Key for clearing messages.
     */
    static final Pattern MESSAGE_PATTERN =
            Pattern.compile("Error in bean[:] (.*?)[,] Line[:] ([0-9]+)[,] Message[:] (.*)");

    @Parameter(alias = "skip", property = "joda.beans.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(alias = "indent", property = "joda.beans.indent")
    private String indent;

    @Parameter(alias = "prefix", property = "joda.beans.prefix")
    private String prefix;

    @Parameter(alias = "config", property = "joda.beans.config")
    private String config;

    @Parameter(alias = "verbose", property = "joda.beans.verbose")
    private Integer verbose;

    @Parameter(alias = "eclipse", property = "joda.beans.eclipse", defaultValue = "false")
    private boolean eclipse;

    @Parameter(alias = "sourceDir", property = "joda.beans.source.dir", defaultValue = "${project.build.sourceDirectory}", required = true)
    private String sourceDir;

    @Parameter(alias = "classesDir", property = "joda.beans.classes.dir", defaultValue = "${project.build.outputDirectory}", required = true, readonly = true)
    private String classesDir;

    @Parameter(alias = "testSourceDir", property = "joda.beans.test.source.dir", defaultValue = "${project.build.testSourceDirectory}", required = true, readonly = true)
    private String testSourceDir;

    @Parameter(alias = "testClassesDir", property = "joda.beans.test.classes.dir", defaultValue = "${project.build.testOutputDirectory}", required = true, readonly = true)
    private String testClassesDir;

    @Parameter(alias = "project", defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Component
    private BuildContext buildContext;

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
     * Gets the classes directory.
     * 
     * @return the classes directory, not null
     */
    protected String getClassesDir() {
        return (classesDir == null ? "" : classesDir.trim());
    }

    /**
     * Gets the test source directory.
     * 
     * @return the test source directory, not null
     */
    protected String getTestSourceDir() {
        return (testSourceDir == null ? "" : testSourceDir.trim());
    }

    /**
     * Gets the test classes directory.
     * 
     * @return the test classes directory, not null
     */
    protected String getTestClassesDir() {
        return (testClassesDir == null ? "" : testClassesDir.trim());
    }

    //-----------------------------------------------------------------------
    /**
     * Executes the Joda-Beans generator.
     * @throws MojoExecutionException if an error occurs
     * @throws MojoFailureException if an error occurs
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
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
        if (config != null) {
            argsList.add("-config=" + config);
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
        // cleanup from last run
        File errorFile = (File) buildContext.getValue(JODA_BEANS_MESSAGE_FILE);
        if (errorFile != null) {
            buildContext.removeMessages(errorFile);
        }
        // invoke main source
        argsList.add(getSourceDir());
        int changedFileCount = runToolHandleChanges(toolClass, argsList, new File(getSourceDir()), new File(getClassesDir()));
        // optionally invoke test source
        if (getTestSourceDir().length() > 0) {
            argsList.set(argsList.size() - 1, getTestSourceDir());
            changedFileCount += runToolHandleChanges(toolClass, argsList, new File(getTestSourceDir()), new File(getTestClassesDir()));
        }
        return changedFileCount;
    }

    private int runToolHandleChanges(Class<?> toolClass, List<String> argsList, File baseDir, File classesDir)
            throws MojoExecutionException, MojoFailureException {
        try {
            String baseStr = baseDir.getCanonicalPath();
            List<File> changedFiles = invoke(toolClass, argsList);
            // mark each file as being in need of a refresh
            if (changedFiles.size() > 0) {
                if (changedFiles.get(0) == null) {
                    buildContext.refresh(baseDir);
                } else {
                    for (File file : changedFiles) {
                        getLog().debug("Refreshed: " + file);
                        buildContext.refresh(file);
                        // when running in Eclipse (determined by the eclipse flag) apply a hack
                        // the hack deleted the class file associated with the java file
                        // this triggers Eclipse to recompile the edited source file
                        // this provides an Eclipse plugin for Joda-Beans just via m2e mechanisms
                        if (eclipse) {
                            String fileStr = file.getCanonicalPath();
                            if (fileStr.length() > baseStr.length() && fileStr.startsWith(baseStr)) {
                                String relative = fileStr.substring(baseStr.length());
                                if (relative.startsWith("/") || relative.startsWith("\\")) {
                                    relative = relative.substring(1);
                                }
                                relative = relative.replace(".java", ".class");
                                File classFile = new File(classesDir, relative);
                                if (classFile.delete()) {
                                    getLog().debug("Deleted: " + classFile);
                                } else {
                                    getLog().debug("Failed to delete: " + classFile);
                                }
                            }
                        }
                    }
                }
            }
            return changedFiles.size();
        } catch (IOException ex) {
            throw new MojoExecutionException("IO problem", ex);
        } catch (MojoFailureException ex) {
            if (eclipse && buildContext.getValue(JODA_BEANS_MESSAGE_FILE) != null) {
                return 0;  // avoid showing error in Eclipse pom that is reported in a file
            } else {
                throw ex;
            }
        }
    }

    private List<File> invoke(Class<?> toolClass, List<String> argsList) throws MojoExecutionException, MojoFailureException {
        Method createFromArgsMethod = findCreateFromArgsMethod(toolClass);
        Method processMethod = findProcessMethod(toolClass);
        Object beanCodeGen = createBuilder(argsList, createFromArgsMethod);
        if (processMethod.getReturnType() == Integer.TYPE) {
            int count = invokeBuilderCountChanges(processMethod, beanCodeGen);
            return Collections.nCopies(count, null);
        } else {
            return invokeBuilderListChanges(processMethod, beanCodeGen);
        }
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
            processMethod = toolClass.getMethod("processFiles");
            getLog().debug("Using Joda-Beans v1.5 or later - processFiles()");
        } catch (Exception ex) {
            try {
                processMethod = toolClass.getMethod("process");
                getLog().debug("Using Joda-Beans v1.4 or earlier - process()");
            } catch (Exception ex2) {
                throw new MojoExecutionException("Unable to find method BeanCodeGen.processFiles() or BeanCodeGen.process()");
            }
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

    private int invokeBuilderCountChanges(Method processMethod, Object beanCodeGen) throws MojoExecutionException, MojoFailureException {
        try {
            return (Integer) processMethod.invoke(beanCodeGen);
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Error invoking BeanCodeGen.process()");
        } catch (IllegalAccessException ex) {
            throw new MojoExecutionException("Error invoking BeanCodeGen.process()");
        } catch (InvocationTargetException ex) {
            throw handleFailure(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private List<File> invokeBuilderListChanges(Method processMethod, Object beanCodeGen) throws MojoExecutionException, MojoFailureException {
        try {
            return (List<File>) processMethod.invoke(beanCodeGen);
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Error invoking BeanCodeGen.process()");
        } catch (IllegalAccessException ex) {
            throw new MojoExecutionException("Error invoking BeanCodeGen.process()");
        } catch (InvocationTargetException ex) {
            throw handleFailure(ex);
        }
    }

    private MojoFailureException handleFailure(InvocationTargetException ex) throws MojoFailureException {
        String msg = ex.getCause().getMessage();
        File file = new File(getSourceDir());
        int line = 1;
        try {
            if (msg.startsWith("Error in bean: ")) {
                Matcher matcher = MESSAGE_PATTERN.matcher(msg);
                if (matcher.matches()) {
                    // Joda-Beans v1.5 messages
                    file = new File(matcher.group(1));
                    line = Integer.parseInt(matcher.group(2));
                    msg = matcher.group(3);
                } else {
                    // Joda-Beans v1.4 messages
                    File sourceFile = new File(msg.substring("Error in bean: ".length()));
                    if (sourceFile.exists()) {
                        file = sourceFile;
                        if (ex.getCause().getCause() != null) {
                            msg = ex.getCause().getCause().getMessage();
                            if (ex.getCause().getCause().getCause() != null) {
                                msg += ": " + ex.getCause().getCause().getCause().getMessage();
                            }
                        }
                    }
                }
            } else if (ex.getCause().getCause() != null) {
                msg += ": " + ex.getCause().getCause().getMessage();
                if (ex.getCause().getCause().getCause() != null) {
                    msg += ": " + ex.getCause().getCause().getCause().getMessage();
                }
            }
        } catch (Exception unexpected) {
            // ignore and use standard messages
        }
        buildContext.setValue(JODA_BEANS_MESSAGE_FILE, file);
        buildContext.addMessage(
                        file.getAbsoluteFile(), line + 1, 1,
                        msg, BuildContext.SEVERITY_ERROR, ex.getCause());
        return new MojoFailureException("Error while running Joda-Beans tool: " + msg, ex.getCause());
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
    private List<String> obtainClasspath() throws MojoExecutionException {
        try {
            return project.getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException ex) {
            throw new MojoExecutionException("Error obtaining dependencies", ex);
        }
    }

}
