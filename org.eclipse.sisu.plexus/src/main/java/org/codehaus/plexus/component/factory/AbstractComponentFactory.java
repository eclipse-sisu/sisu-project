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
