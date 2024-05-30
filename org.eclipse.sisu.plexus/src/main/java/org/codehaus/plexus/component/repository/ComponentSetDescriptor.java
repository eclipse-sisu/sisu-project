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
package org.codehaus.plexus.component.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentSetDescriptor
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private boolean isolatedRealm;

    private List<ComponentDescriptor<?>> components = Collections.emptyList();

    private List<ComponentDependency> dependencies = Collections.emptyList();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void setIsolatedRealm( final boolean isolatedRealm )
    {
        this.isolatedRealm = isolatedRealm;
    }

    public final void addComponentDescriptor( final ComponentDescriptor<?> component )
    {
        if ( components.isEmpty() )
        {
            components = new ArrayList<ComponentDescriptor<?>>();
        }
        components.add( component );
    }

    public final void setComponents( final List<ComponentDescriptor<?>> components )
    {
        this.components = new ArrayList<ComponentDescriptor<?>>( components );
    }

    public final void addDependency( final ComponentDependency dependency )
    {
        if ( dependencies.isEmpty() )
        {
            dependencies = new ArrayList<ComponentDependency>();
        }
        dependencies.add( dependency );
    }

    public final void setDependencies( final List<ComponentDependency> dependencies )
    {
        this.dependencies = new ArrayList<ComponentDependency>( dependencies );
    }

    public final boolean isIsolatedRealm()
    {
        return isolatedRealm;
    }

    public final List<ComponentDescriptor<?>> getComponents()
    {
        return components;
    }

    public final List<ComponentDependency> getDependencies()
    {
        return dependencies;
    }

    @Override
    public final String toString()
    {
        final StringBuilder buf = new StringBuilder( "Component Descriptor: " );
        for ( final ComponentDescriptor<?> cd : components )
        {
            buf.append( cd.getHumanReadableKey() ).append( "\n" );
        }
        return buf.append( "---" ).toString();
    }

    @SuppressWarnings( "unused" )
    public final void setId( final String id )
    {
        // ignore
    }
}
