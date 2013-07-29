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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.joda.beans.gen.BeanCodeGen;

/**
 * Maven plugin for validating that the generated Joda-Beans are up to date.
 * 
 * @goal validate
 * @phase process-sources
 */
public class JodaBeansValidateMojo extends AbstractMojo {

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
     * @parameter alias="stopOnError" property="joda.beans.stopOnError"
     */
    private boolean stopOnError = true;
    /**
     * @parameter property="project.build.sourceDirectory"
     * @required
     * @readonly
     */
    private String sourceDir;

    //-----------------------------------------------------------------------
    /**
     * Executes the Joda-Beans generator, validating that there are no changes.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sourceDir == null) {
            throw new MojoExecutionException("Source directory must not be null");
        }
        
        // build args
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
        argsList.add("-nowrite");
        argsList.add(sourceDir);
        
        // run generator without writing
        getLog().info("Joda-Bean validator started, directory: " + sourceDir);
        BeanCodeGen gen = null;
        try {
            String[] args = argsList.toArray(new String[argsList.size()]);
            gen = BeanCodeGen.createFromArgs(args);
        } catch (RuntimeException ex) {
            throw new MojoFailureException("Invalid Joda-Beans Mojo configuration: " + ex.getMessage(), ex);
        }
        int changes = 0;
        try {
            changes = gen.process();
        } catch (Exception ex) {
            throw new MojoFailureException("Error while running Joda-Beans generator: " + ex.getMessage(), ex);
        }
        if (changes > 0) {
            if (stopOnError) {
                throw new MojoFailureException("Some Joda-Beans need to be re-generated (" + changes + " files)");
            }
            getLog().info("*** Joda-Bean validator found " + changes + " beans in need of generation ***");
        } else {
            getLog().info("Joda-Bean validator completed");
        }
    }

}
