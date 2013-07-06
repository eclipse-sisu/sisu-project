/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.IndexedClassFinder;

import com.google.inject.Binder;
import com.google.inject.Module;

public final class SisuExtensions
{
    private final ClassSpace space;

    private final boolean global;

    private SisuExtensions( final ClassSpace space, final boolean global )
    {
        this.space = space;
        this.global = global;
    }

    public static SisuExtensions local( final ClassSpace space )
    {
        return new SisuExtensions( space, false );
    }

    public static SisuExtensions global( final ClassSpace space )
    {
        return new SisuExtensions( space, true );
    }

    public <T> Iterable<T> load( final Class<T> api, final Binder binder )
    {
        final List<T> extensions = new ArrayList<T>();
        final String index = "META-INF/services/" + api.getName();
        for ( final String name : new IndexedClassFinder( index, global ).indexedNames( space ) )
        {
            try
            {
                final T extension;
                final Class<?> impl = space.loadClass( name );
                if ( Module.class.isAssignableFrom( impl ) )
                {
                    extension = api.cast( impl.newInstance() );
                    binder.install( (Module) extension );
                }
                else
                {
                    extension = api.cast( impl.getConstructor( Binder.class ).newInstance( binder ) );
                }
                extensions.add( extension );
            }
            catch ( final Exception e )
            {
                Logs.trace( "Problem loading: {}", name, e );
            }
            catch ( final LinkageError e )
            {
                Logs.trace( "Problem loading: {}", name, e );
            }
        }
        return extensions;
    }
}
