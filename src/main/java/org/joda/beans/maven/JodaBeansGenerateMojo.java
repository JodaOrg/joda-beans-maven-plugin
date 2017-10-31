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

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Maven plugin for generating Joda-Beans.
 * 
 * @goal generate
 * @phase process-sources
 * @requiresDependencyResolution compile
 */
public class JodaBeansGenerateMojo extends AbstractJodaBeansMojo {

    //-----------------------------------------------------------------------
    @Override
    protected int runTool(Class<?> toolClass, List<String> argsList) throws MojoExecutionException, MojoFailureException {
        getLog().info("Joda-Bean generator started, directory: " + getSourceDir() +
                        (getTestSourceDir().length() == 0 ? "" : ", test directory:" + getTestSourceDir()));
        int changes = super.runTool(toolClass, argsList);
        getLog().info("Joda-Bean generator completed, " + changes + " changed files");
        return changes;
    }

}
