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
package org.codehaus.plexus;

import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

public final class DefaultContainerConfiguration
    implements ContainerConfiguration
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String configurationPath;

    private URL configurationUrl;

    private ClassWorld classWorld;

    private ClassRealm classRealm;

    private Map<Object, Object> contextData;

    private String componentVisibility = PlexusConstants.REALM_VISIBILITY;

    private String classPathScanning = PlexusConstants.SCANNING_OFF;

    private boolean autoWiring;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public ContainerConfiguration setName( final String name )
    {
        return this;
    }

    public ContainerConfiguration setContainerConfiguration( final String configurationPath )
    {
        this.configurationPath = configurationPath;
        return this;
    }

    public String getContainerConfiguration()
    {
        return configurationPath;
    }

    public ContainerConfiguration setContainerConfigurationURL( final URL configurationUrl )
    {
        this.configurationUrl = configurationUrl;
        return this;
    }

    public URL getContainerConfigurationURL()
    {
        return configurationUrl;
    }

    public ContainerConfiguration setClassWorld( final ClassWorld classWorld )
    {
        this.classWorld = classWorld;
        return this;
    }

    public ClassWorld getClassWorld()
    {
        return classWorld;
    }

    public ContainerConfiguration setRealm( final ClassRealm classRealm )
    {
        this.classRealm = classRealm;
        return this;
    }

    public ClassRealm getRealm()
    {
        return classRealm;
    }

    public ContainerConfiguration setContext( final Map<Object, Object> contextData )
    {
        this.contextData = contextData;
        return this;
    }

    public Map<Object, Object> getContext()
    {
        return contextData;
    }

    public ContainerConfiguration setComponentVisibility( final String componentVisibility )
    {
        this.componentVisibility = componentVisibility;
        return this;
    }

    public String getComponentVisibility()
    {
        return componentVisibility;
    }

    public ContainerConfiguration setClassPathScanning( final String classPathScanning )
    {
        this.classPathScanning = classPathScanning;
        if ( !PlexusConstants.SCANNING_OFF.equalsIgnoreCase( classPathScanning ) )
        {
            autoWiring = true;
        }
        return this;
    }

    public String getClassPathScanning()
    {
        return classPathScanning;
    }

    public ContainerConfiguration setAutoWiring( final boolean autoWiring )
    {
        this.autoWiring = autoWiring;
        return this;
    }

    public boolean getAutoWiring()
    {
        return autoWiring;
    }
}
