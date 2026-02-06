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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.build.BuildContext;

/**
 * Generates a qualified class index for test classes compiled by the current project.
 */
@Mojo(
        name = "test-index",
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true)
public class TestIndexMojo extends AbstractMojo {
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

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void execute() {
        final IndexMojo mojo = new IndexMojo(buildContext);
        mojo.setLog(getLog());
        mojo.setProject(project);
        mojo.setOutputDirectory(new File(project.getBuild().getTestOutputDirectory()));
        mojo.execute();
    }
}
