/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu.mojos;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ClassifierFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.GroupIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ProjectTransitivityFilter;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;
import org.codehaus.plexus.build.BuildContext;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.io.CachingWriter;
import org.eclipse.sisu.space.SisuIndex;
import org.eclipse.sisu.space.URLClassSpace;

/**
 * Generates a qualified class index for the current project and its dependencies.
 */
@Mojo(name = "index", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class IndexMojo extends AbstractMojo {
    static final String INDEX_FOLDER = "META-INF/sisu/"; // copied from AbstractSisuIndex as not public

    // ----------------------------------------------------------------------
    // Configurable parameters
    // ----------------------------------------------------------------------

    /**
     * The output directory.
     */
    @Parameter(property = "outputDirectory", defaultValue = "${project.build.outputDirectory}")
    protected File outputDirectory;

    /**
     * If we should include project dependencies when indexing.
     */
    @Parameter(property = "includeDependencies", defaultValue = "true")
    protected boolean includeDependencies;

    /**
     * Comma separated list of GroupIds to exclude when indexing.
     */
    @Parameter(property = "excludeGroupIds", defaultValue = "")
    protected String excludeGroupIds;

    /**
     * Comma separated list of GroupIds to include when indexing.
     */
    @Parameter(property = "includeGroupIds", defaultValue = "")
    protected String includeGroupIds;

    /**
     * Comma separated list of ArtifactIds to exclude when indexing.
     */
    @Parameter(property = "excludeArtifactIds", defaultValue = "")
    protected String excludeArtifactIds;

    /**
     * Comma separated list of ArtifactIds to include when indexing.
     */
    @Parameter(property = "includeArtifactIds", defaultValue = "")
    protected String includeArtifactIds;

    /**
     * Comma Separated list of Classifiers to exclude when indexing.
     */
    @Parameter(property = "excludeClassifiers", defaultValue = "")
    protected String excludeClassifiers;

    /**
     * Comma Separated list of Classifiers to include when indexing.
     */
    @Parameter(property = "includeClassifiers", defaultValue = "")
    protected String includeClassifiers;

    /**
     * Comma Separated list of Types to exclude when indexing.
     */
    @Parameter(property = "excludeTypes", defaultValue = "")
    protected String excludeTypes;

    /**
     * Comma Separated list of Types to include when indexing.
     */
    @Parameter(property = "includeTypes", defaultValue = "")
    protected String includeTypes;

    /**
     * Scope to exclude. Empty string indicates no scopes (default).
     */
    @Parameter(property = "excludeScope", defaultValue = "")
    protected String excludeScope;

    /**
     * Scope to include. Empty string indicates all scopes (default).
     */
    @Parameter(property = "includeScope", defaultValue = "")
    protected String includeScope;

    /**
     * If we should exclude transitive dependencies when indexing.
     */
    @Parameter(property = "excludeTransitive", defaultValue = "false")
    protected boolean excludeTransitive;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    /**
     * The Maven project to index.
     */
    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * For m2e incremental build support
     */
    @Component
    protected BuildContext buildContext;

    public IndexMojo() {
        super();
    }

    public IndexMojo(final BuildContext buildContext) {
        super();
        this.buildContext = buildContext;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setProject(final MavenProject project) {
        this.project = project;
    }

    public void setOutputDirectory(final File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void execute() {
        synchronized (project) {
            new SisuIndex(outputDirectory) {
                @Override
                protected Writer getWriter(String path) throws IOException {
                    Path p = outputDirectory.toPath().resolve(path);
                    Path d = p.getParent();
                    if (!Files.isDirectory(d)) {
                        Files.createDirectories(d);
                    }
                    return new CachingWriter(p, StandardCharsets.UTF_8);
                }

                @Override
                protected void info(final String message) {
                    getLog().info(message);
                }

                @Override
                protected void warn(final String message) {
                    getLog().warn(message);
                }
            }.index(new URLClassSpace(getProjectClassLoader(), getIndexPath()));
            buildContext.refresh(new File(outputDirectory, INDEX_FOLDER));
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private ClassLoader getProjectClassLoader() {
        final List<URL> classPath = new ArrayList<>();
        appendDirectoryToClassPath(classPath, outputDirectory);
        for (final Object artifact : project.getArtifacts()) {
            appendFileToClassPath(classPath, ((Artifact) artifact).getFile());
        }
        if (getLog().isDebugEnabled()) {
            dumpEntries("classPath", classPath);
        }
        return URLClassLoader.newInstance(classPath.toArray(new URL[classPath.size()]));
    }

    private URL[] getIndexPath() {
        final List<URL> indexPath = new ArrayList<>();
        appendDirectoryToClassPath(indexPath, outputDirectory);
        if (includeDependencies && !buildContext.isIncremental()) {
            final FilterArtifacts filter = new FilterArtifacts();

            filter.addFilter(new ProjectTransitivityFilter(project.getDependencyArtifacts(), excludeTransitive));
            filter.addFilter(new ScopeFilter(cleanList(includeScope), cleanList(excludeScope)));
            filter.addFilter(new TypeFilter(cleanList(includeTypes), cleanList(excludeTypes)));
            filter.addFilter(new ClassifierFilter(cleanList(includeClassifiers), cleanList(excludeClassifiers)));
            filter.addFilter(new GroupIdFilter(cleanList(includeGroupIds), cleanList(excludeGroupIds)));
            filter.addFilter(new ArtifactIdFilter(cleanList(includeArtifactIds), cleanList(excludeArtifactIds)));

            try {
                for (final Object artifact : filter.filter(project.getArtifacts())) {
                    appendFileToClassPath(indexPath, ((Artifact) artifact).getFile());
                }
            } catch (final ArtifactFilterException e) {
                getLog().warn(e.getLocalizedMessage());
            }
        }
        if (getLog().isDebugEnabled()) {
            dumpEntries("indexPath", indexPath);
        }
        return indexPath.toArray(new URL[indexPath.size()]);
    }

    private void dumpEntries(final String name, final List<URL> urls) {
        getLog().debug(name + " entries: (" + urls.size() + ")");
        for (int i = 1; i <= urls.size(); i++) {
            getLog().debug(i + ". " + urls.get(i - 1));
        }
    }

    private void appendDirectoryToClassPath(final List<URL> urls, File directory) {
        if (directory.isDirectory()) {
            Scanner scanner = buildContext.newScanner(directory);
            scanner.setIncludes(new String[] {"**/*.class"});
            scanner.scan();
            String[] includedFiles = scanner.getIncludedFiles();
            if (includedFiles != null && includedFiles.length > 0) {
                getLog().debug("Found at least one class file in " + directory);
                appendFileToClassPath(urls, directory);
            } else {
                getLog().debug("No class files found in " + directory);
            }
        } else {
            getLog().debug("Path " + directory + " does not exist or is no directory");
        }
    }

    /**
     *
     * @param urls the list to which to append the URL
     * @param file must either be a directory or a JAR file
     */
    private void appendFileToClassPath(final List<URL> urls, final File file) {
        if (null != file) {
            try {
                urls.add(file.toURI().toURL());
            } catch (final MalformedURLException e) {
                getLog().warn(e.getLocalizedMessage());
            }
        }
    }

    private static String cleanList(final String list) {
        return StringUtils.isEmpty(list) ? "" : StringUtils.join(StringUtils.split(list), ",");
    }
}
