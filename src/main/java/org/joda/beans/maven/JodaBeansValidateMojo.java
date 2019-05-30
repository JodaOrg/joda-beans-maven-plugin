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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Maven plugin for validating that the generated Joda-Beans are up to date.
 */
@Mojo(name = "validate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true)
@Execute(goal = "validate", phase = LifecyclePhase.GENERATE_SOURCES)
public class JodaBeansValidateMojo extends AbstractJodaBeansMojo {

    @Parameter(alias = "stopOnError", property = "joda.beans.stopOnError", defaultValue = "true")
    private boolean stopOnError = true;

    //-----------------------------------------------------------------------
    @Override
    protected List<String> buildArgs() {
        List<String> argsList = super.buildArgs();
        argsList.add("-nowrite");
        return argsList;
    }

    @Override
    protected void runTool(
            Class<?> toolClass,
            List<String> argsList,
            BuildContext buildContext) throws MojoExecutionException, MojoFailureException {

        logInfo("Joda-Bean validator started, directory: " + getSourceDir() +
                        (getTestSourceDir().length() == 0 ? "" : ", test directory:" + getTestSourceDir()));
        List<File> changedFiles = runTool(toolClass, argsList);
        if (changedFiles.size() > 0) {
            if (stopOnError) {
                changedFiles.forEach(file -> getLog().warn("Joda-Bean needs to be re-generated: " + file));
                throw new MojoFailureException("Some Joda-Beans need to be re-generated (" + changedFiles.size() + " files)");
            }
            logInfo("*** Joda-Bean validator found " + changedFiles.size() + " beans in need of generation ***");
        } else {
            logInfo("Joda-Bean validator completed");
        }
    }

    private List<File> runTool(Class<?> toolClass, List<String> argsList) throws MojoExecutionException, MojoFailureException {
        // invoke main source
        argsList.add(getSourceDir());
        List<File> changedFiles = new ArrayList<>();

        List<File> productionFilesChanged = runToolHandleChanges(toolClass, argsList, new File(getSourceDir()), new File(getClassesDir()));
        changedFiles.addAll(productionFilesChanged);

        // optionally invoke test source
        if (getTestSourceDir().length() > 0) {
            argsList.set(argsList.size() - 1, getTestSourceDir());
            List<File> testFilesChanged = runToolHandleChanges(toolClass, argsList, new File(getTestSourceDir()), new File(getTestClassesDir()));
            changedFiles.addAll(testFilesChanged);
        }
        return changedFiles;
    }

}
