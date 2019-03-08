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
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Maven plugin for generating Joda-Beans.
 */
public abstract class AbstractJodaBeansGenerateMojo extends AbstractJodaBeansMojo {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    //-----------------------------------------------------------------------
    @Override
    protected void runTool(
            Class<?> toolClass,
            List<String> argsList,
            BuildContext buildContext) throws MojoExecutionException, MojoFailureException {

        // only match java files that have changed according to the build context
        // this avoids processing when there is nothing to do
        String[] changedSourceFiles = findFiles(buildContext, getSourceDir());
        String[] changedTestFiles = findFiles(buildContext, getTestSourceDir());
        int sourceFilesChanged = changedSourceFiles.length;
        int testFilesChanged = changedTestFiles.length;

        // if nothing to do then exit
        if (sourceFilesChanged == 0 && testFilesChanged == 0) {
            logInfo("No files changed");
            return;
        }
        logDebug("Files changed: main=" + sourceFilesChanged + ", test=" + testFilesChanged);

        logInfo("Joda-Bean generator started, directory: " + getSourceDir() +
                (getTestSourceDir().length() == 0 ? "" : ", test directory: " + getTestSourceDir()));

        // invoke main source
        int changedFileCount = 0;
        if (sourceFilesChanged > 0) {
            File sourceDir = new File(getSourceDir());
            File classesDir = new File(getClassesDir());
            for (String changedFile : changedSourceFiles) {
                buildContext.removeMessages(new File(sourceDir, changedFile));
            }
            if (sourceFilesChanged == 1) {
                File file = new File(sourceDir, changedSourceFiles[0]);
                argsList.add(file.toString());
                logDebug("Single file: " + argsList.get(argsList.size() - 1));
                changedFileCount += runToolHandleChanges(toolClass, argsList, sourceDir, classesDir);
            } else {
                argsList.add(getSourceDir());
                logDebug("All files: " + argsList.get(argsList.size() - 1));
                changedFileCount += runToolHandleChanges(toolClass, argsList, sourceDir, classesDir);
            }
        }
        // optionally invoke test source
        if (testFilesChanged > 0) {
            File sourceDir = new File(getTestSourceDir());
            File classesDir = new File(getTestClassesDir());
            for (String changedFile : changedTestFiles) {
                buildContext.removeMessages(new File(sourceDir, changedFile));
            }
            if (testFilesChanged == 1) {
                File file = new File(sourceDir, changedTestFiles[0]);
                argsList.set(argsList.size() - 1, file.toString());
                logDebug("Single test file: " + argsList.get(argsList.size() - 1));
                changedFileCount += runToolHandleChanges(toolClass, argsList, sourceDir, classesDir);
            } else {
                argsList.set(argsList.size() - 1, getTestSourceDir());
                logDebug("All test files: " + argsList.get(argsList.size() - 1));
                changedFileCount += runToolHandleChanges(toolClass, argsList, sourceDir, classesDir);
            }
        }

        logInfo("Joda-Bean generator completed, " + changedFileCount + " changed files");
    }

    // find Java files
    private String[] findFiles(BuildContext buildContext, String dirStr) {
        File dir = new File(dirStr);
        if (dirStr.isEmpty() || !dir.exists()) {
            return EMPTY_STRING_ARRAY;
        }
        Scanner scanner = buildContext.newScanner(dir);
        scanner.setIncludes(new String[] {"**/*.java"});
        scanner.scan();
        String[] changedSourceFiles = scanner.getIncludedFiles();
        return changedSourceFiles;
    }

}
