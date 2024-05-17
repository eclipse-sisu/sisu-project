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
package org.codehaus.plexus;

import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.context.Context;

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

    private Context contextComponent;

    private boolean jsr250Lifecycle;

    private boolean strictClassPathScanning;

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

    public ContainerConfiguration setContextComponent( final Context contextComponent )
    {
        this.contextComponent = contextComponent;
        return this;
    }

    public Context getContextComponent()
    {
        return contextComponent;
    }

    public ContainerConfiguration setJSR250Lifecycle( final boolean jsr250Lifecycle )
    {
        this.jsr250Lifecycle = jsr250Lifecycle;
        return this;
    }

    public boolean getJSR250Lifecycle()
    {
        return jsr250Lifecycle;
    }

    @Override
    public ContainerConfiguration setStrictClassPathScanning( boolean strictScanning )
    {
        this.strictClassPathScanning = strictScanning;
        return this;
    }

    @Override
    public boolean getStrictClassPathScanning()
    {
        return strictClassPathScanning;
    }
}
