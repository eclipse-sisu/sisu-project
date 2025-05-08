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
package org.codehaus.plexus.personality.plexus.lifecycle.phase;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public final class PlexusContainerLocator
    implements ServiceLocator
{
    private final PlexusContainer container;

    public PlexusContainerLocator( final PlexusContainer container )
    {
        this.container = container;
    }

    public Object lookup( final String role )
        throws ComponentLookupException
    {
        return container.lookup( role );
    }

    public Object lookup( final String role, final String hint )
        throws ComponentLookupException
    {
        return container.lookup( role, hint );
    }

    public Map<String, Object> lookupMap( final String role )
        throws ComponentLookupException
    {
        return container.lookupMap( role );
    }

    public List<Object> lookupList( final String role )
        throws ComponentLookupException
    {
        return container.lookupList( role );
    }

    public void release( final Object component )
        throws ComponentLifecycleException
    {
        container.release( component );
    }

    public void releaseAll( final Map<String, ?> components )
        throws ComponentLifecycleException
    {
        container.releaseAll( components );
    }

    public void releaseAll( final List<?> components )
        throws ComponentLifecycleException
    {
        container.releaseAll( components );
    }

    public boolean hasComponent( final String role )
    {
        return container.hasComponent( role );
    }

    public boolean hasComponent( final String role, final String hint )
    {
        return container.hasComponent( role, hint );
    }
}
