/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus.component.repository;

public final class ComponentDependency
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String groupId;

    private String artifactId;

    private String version;

    private String type = "jar";

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setGroupId( final String groupId )
    {
        this.groupId = groupId;
    }

    public void setArtifactId( final String artifactId )
    {
        this.artifactId = artifactId;
    }

    public void setVersion( final String version )
    {
        this.version = version;
    }

    public void setType( final String type )
    {
        this.type = type;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return "groupId = " + groupId + ", artifactId = " + artifactId + ", version = " + version + ", type = " + type;
    }
}
