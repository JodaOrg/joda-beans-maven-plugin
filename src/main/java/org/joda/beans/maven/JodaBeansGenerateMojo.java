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
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Maven plugin for generating Joda-Beans.
 */
@Mojo(name = "generate",
        defaultPhase = LifecyclePhase.PROCESS_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true)
@Execute(goal = "generate", phase = LifecyclePhase.PROCESS_SOURCES)
public class JodaBeansGenerateMojo extends AbstractJodaBeansMojo {

    //-----------------------------------------------------------------------
    @Override
    protected void runTool(
            Class<?> toolClass,
            List<String> argsList,
            BuildContext buildContext) throws MojoExecutionException, MojoFailureException {

        // only match java files that have changed according to the build context
        // this avoids processing when there is nothing to do
        Scanner scanner = buildContext.newScanner(new File(getSourceDir()));
        scanner.setIncludes(new String[] {"**/*.java"});
        scanner.scan();
        String[] changedSourceFiles = scanner.getIncludedFiles();
        int sourceFilesChanged = changedSourceFiles == null ? 0 : changedSourceFiles.length;
        int testFilesChanged = 0;
        if (getTestSourceDir().length() > 0) {
            Scanner testFilesScanner = buildContext.newScanner(new File(getTestSourceDir()));
            testFilesScanner.setIncludes(new String[] {"**/*.java"});
            testFilesScanner.scan();
            String[] changedTestFiles = testFilesScanner.getIncludedFiles();
            testFilesChanged = changedTestFiles == null ? 0 : changedTestFiles.length;
        }

        // if nothing to do then exit
        if (sourceFilesChanged == 0 && testFilesChanged == 0) {
            logInfo("No files changed");
            return;
        }
        logDebug("Files changed: main=" + sourceFilesChanged + ", test=" + testFilesChanged);

        logInfo("Joda-Bean generator started, directory: " + getSourceDir() +
                        (getTestSourceDir().length() == 0 ? "" : ", test directory:" + getTestSourceDir()));

        // invoke main source
        int changedFileCount = 0;
        if (sourceFilesChanged > 0) {
            if (sourceFilesChanged == 1) {
                File file = new File(getSourceDir(), changedSourceFiles[0]);
                argsList.add(file.toString());
                logDebug("Single file: " + argsList.get(argsList.size() - 1));
                changedFileCount += runToolHandleChanges(toolClass, argsList, file.getParentFile(), new File(getClassesDir()));
            } else {
                argsList.add(getSourceDir());
                logDebug("All files: " + argsList.get(argsList.size() - 1));
                changedFileCount += runToolHandleChanges(toolClass, argsList, new File(getSourceDir()), new File(getClassesDir()));
            }
        }
        // optionally invoke test source
        if (testFilesChanged > 0 && getTestSourceDir().length() > 0) {
            if (sourceFilesChanged == 1) {
                File file = new File(getSourceDir(), changedSourceFiles[0]);
                argsList.set(argsList.size() - 1, file.toString());
                logDebug("Single test file: " + argsList.get(argsList.size() - 1));
                changedFileCount += runToolHandleChanges(toolClass, argsList, file.getParentFile(), new File(getTestClassesDir()));
            } else {
                argsList.set(argsList.size() - 1, getTestSourceDir());
                logDebug("All test files: " + argsList.get(argsList.size() - 1));
                changedFileCount += runToolHandleChanges(toolClass, argsList, new File(getTestSourceDir()), new File(getTestClassesDir()));
            }
        }

        logInfo("Joda-Bean generator completed, " + changedFileCount + " changed files");
    }

}
