/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
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
package org.codehaus.plexus.component.configurator;

import org.codehaus.plexus.configuration.PlexusConfiguration;

public final class ComponentConfigurationException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    private PlexusConfiguration configuration;

    public ComponentConfigurationException( final String message )
    {
        super( message );
    }

    public ComponentConfigurationException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

    public ComponentConfigurationException( final Throwable cause )
    {
        super( cause );
    }

    public ComponentConfigurationException( final PlexusConfiguration configuration, final String message )
    {
        super( message );
        setFailedConfiguration( configuration );
    }

    public ComponentConfigurationException( final PlexusConfiguration configuration, final String message,
                                            final Throwable cause )
    {
        super( message, cause );
        setFailedConfiguration( configuration );
    }

    public ComponentConfigurationException( final PlexusConfiguration configuration, final Throwable cause )
    {
        super( cause );
        setFailedConfiguration( configuration );
    }

    public void setFailedConfiguration( final PlexusConfiguration configuration )
    {
        this.configuration = configuration;
    }

    public PlexusConfiguration getFailedConfiguration()
    {
        return configuration;
    }
}
