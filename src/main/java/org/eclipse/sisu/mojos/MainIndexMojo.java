/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.mojos;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * Generates a qualified class index for classes compiled by the current project.
 * 
 * @goal main-index
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class MainIndexMojo
    extends AbstractMojo
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    /**
     * The Maven project to index.
     * 
     * @parameter property="project"
     * @required
     * @readonly
     */
    private MavenProject project;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void execute()
    {
        final IndexMojo mojo = new IndexMojo();
        mojo.setLog( getLog() );
        mojo.setProject( project );
        mojo.setOutputDirectory( new File( project.getBuild().getOutputDirectory() ) );
        mojo.execute();
    }
}
