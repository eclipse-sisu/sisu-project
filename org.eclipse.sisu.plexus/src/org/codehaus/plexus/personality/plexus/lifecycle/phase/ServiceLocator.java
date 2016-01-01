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
package org.codehaus.plexus.personality.plexus.lifecycle.phase;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public interface ServiceLocator
{
    Object lookup( String role )
        throws ComponentLookupException;

    Object lookup( String role, String hint )
        throws ComponentLookupException;

    Map<String, Object> lookupMap( String role )
        throws ComponentLookupException;

    List<Object> lookupList( String role )
        throws ComponentLookupException;

    void release( Object component )
        throws ComponentLifecycleException;

    void releaseAll( Map<String, ?> components )
        throws ComponentLifecycleException;

    void releaseAll( List<?> components )
        throws ComponentLifecycleException;

    boolean hasComponent( String role );

    boolean hasComponent( String role, String hint );
}
