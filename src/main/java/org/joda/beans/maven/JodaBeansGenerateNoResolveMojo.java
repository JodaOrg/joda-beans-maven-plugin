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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.RepositorySystem;

/**
 * Maven plugin for generating Joda-Beans without dependency resolution.
 */
@Mojo(name = "generate-no-resolve", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
@Execute(goal = "generate-no-resolve", phase = LifecyclePhase.PROCESS_SOURCES)
public class JodaBeansGenerateNoResolveMojo extends AbstractJodaBeansGenerateMojo {

    private static ClassLoader cached = null;

    @Parameter(alias = "jodaBeansVersion", property = "joda.beans.version", defaultValue = "2.5.0", required = true)
    private String jodaBeansVersion;

    @Component
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", required = true, readonly = true)
    private List<ArtifactRepository> remoteRepos;

    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    private ArtifactRepository localRepo;

    @Override
    synchronized ClassLoader obtainClassLoader() throws MojoExecutionException {
        if (cached != null) {
            return cached;
        }
        logInfo("Finding joda-beans for version " + jodaBeansVersion + " (override using property: joda.beans.version)");
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setResolveTransitively(true);
        request.setLocalRepository(localRepo);
        request.setRemoteRepositories(remoteRepos);
        request.setArtifact(repoSystem.createArtifact("org.joda", "joda-beans", jodaBeansVersion, "compile", "jar"));
        ArtifactResolutionResult result = repoSystem.resolve(request);
        if (!result.isSuccess()) {
            throw new MojoExecutionException("Unable to resolve org.joda:joda-beans:" + jodaBeansVersion);
        }
        List<URL> classpath = new ArrayList<>();
        for (Artifact artifact : result.getArtifacts()) {
            File file = new File(localRepo.getBasedir(), localRepo.pathOf(artifact));
            try {
                URL location = file.toURI().toURL();
                classpath.add(location);
                logDebug("at " + location);
            } catch (MalformedURLException ex) {
                throw new RuntimeException("Error interpreting classpath entry as URL: " + file, ex);
            }
        }
        URL[] classpathUrls = classpath.toArray(new URL[classpath.size()]);
        URLClassLoader classLoader = new URLClassLoader(classpathUrls, AbstractJodaBeansMojo.class.getClassLoader());
        cached = classLoader;
        return classLoader;
    }

}
