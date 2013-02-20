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
 * Maven plugin for running Joda-Beans.
 * 
 * @goal generate
 * @phase generate-sources
 * 
 * @author Stephen Colebourne
 */
public class JodaBeansGenerateMojo extends AbstractMojo {

    /**
     * @parameter alias="indent"
     */
    private String indent;
    /**
     * @parameter alias="prefix"
     */
    private String prefix;
    /**
     * @parameter alias="verbose"
     */
    private Integer verbose;
    /**
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     * @readonly
     */
    private String sourceDir;
//    /**
//     * @parameter expression="${descriptor}"
//     * @required
//     * @readonly
//     */
//    private PluginDescriptor descriptor;
//    /**
//     * @parameter expression="${project}"
//     * @required
//     * @readonly
//     */
//    private MavenProject project;

    /**
     * Executes the Joda-Beans generator.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sourceDir == null) {
            throw new MojoExecutionException("Source directory must not be null");
        }
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
        argsList.add(sourceDir);
        
        getLog().info("Joda-Bean generator started, directory: " + sourceDir);
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
        getLog().info("Joda-Bean generator completed, " + changes + " changed files");
    }

}
