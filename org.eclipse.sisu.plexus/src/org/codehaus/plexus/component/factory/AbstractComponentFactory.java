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
package org.codehaus.plexus.component.factory;

import org.codehaus.classworlds.ClassRealmAdapter;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

@SuppressWarnings( { "rawtypes", "deprecation" } )
public abstract class AbstractComponentFactory
    implements ComponentFactory
{
    public Object newInstance( final ComponentDescriptor cd, final ClassRealm realm, final PlexusContainer container )
        throws ComponentInstantiationException
    {
        return newInstance( cd, ClassRealmAdapter.getInstance( realm ), container );
    }

    @SuppressWarnings( "unused" )
    protected Object newInstance( final ComponentDescriptor cd, final org.codehaus.classworlds.ClassRealm realm,
                                  final PlexusContainer container )
        throws ComponentInstantiationException
    {
        throw new IllegalStateException( getClass() + " does not implement component creation" );
    }
}
