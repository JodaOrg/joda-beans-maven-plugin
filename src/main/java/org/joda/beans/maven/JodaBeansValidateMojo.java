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

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Maven plugin for validating that the generated Joda-Beans are up to date.
 * 
 * @goal validate
 * @phase process-sources
 * @requiresDependencyResolution compile
 */
public class JodaBeansValidateMojo extends AbstractJodaBeansMojo {

    /**
     * @parameter alias="stopOnError" property="joda.beans.stopOnError"
     */
    private boolean stopOnError = true;

    //-----------------------------------------------------------------------
    @Override
    protected List<String> buildArgs() {
        List<String> argsList = super.buildArgs();
        argsList.add("-nowrite");
        return argsList;
    }

    @Override
    protected int runTool(Class<?> toolClass, List<String> argsList) throws MojoExecutionException, MojoFailureException {
        getLog().info("Joda-Bean validator started, directory: " + getSourceDir() +
                        (getTestSourceDir().length() == 0 ? "" : ", test directory:" + getTestSourceDir()));
        int changes = super.runTool(toolClass, argsList);
        if (changes > 0) {
            if (stopOnError) {
                throw new MojoFailureException("Some Joda-Beans need to be re-generated (" + changes + " files)");
            }
            getLog().info("*** Joda-Bean validator found " + changes + " beans in need of generation ***");
        } else {
            getLog().info("Joda-Bean validator completed");
        }
        return changes;
    }

}
