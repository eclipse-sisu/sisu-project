/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
