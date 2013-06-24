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

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sisu.bean.DeclaredMembers;
import org.eclipse.sisu.bean.DeclaredMembers.View;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.IndexedClassFinder;

public class Extensions
{
    private final ClassSpace space;

    public Extensions( final ClassSpace space )
    {
        this.space = space;
    }

    public <T> Iterable<T> load( final Class<T> api, final Object... args )
    {
        final List<T> instances = new ArrayList<T>();
        final String index = "META-INF/services/" + api.getName();
        for ( final String s : new IndexedClassFinder( index, false ).indexedNames( space ) )
        {
            try
            {
                @SuppressWarnings( "unchecked" )
                final Class<T> impl = (Class<T>) space.loadClass( s );
                if ( api.isAssignableFrom( impl ) )
                {
                    instances.add( newInstance( impl, args ) );
                }
            }
            catch ( Exception e )
            {
                Logs.trace( "Problem loading extension {}", s, e );
            }
            catch ( LinkageError e )
            {
                Logs.trace( "Problem loading extension {}", s, e );
            }
        }
        return instances;
    }

    private static <T> T newInstance( final Class<T> impl, final Object... args )
        throws Exception
    {
        for ( final Member m : new DeclaredMembers( impl, View.CONSTRUCTORS ) )
        {
            @SuppressWarnings( "unchecked" )
            final Constructor<T> ctor = (Constructor<T>) m;
            final Class<?>[] types = ctor.getParameterTypes();
            if ( args.length != types.length )
            {
                continue; // wrong number of parameters
            }
            for ( int i = 0; i < types.length; i++ )
            {
                if ( !types[i].isInstance( args[i] ) )
                {
                    continue; // incompatible parameters
                }
            }
            return ctor.newInstance( args );
        }
        return impl.newInstance(); // ignore parameters
    }
}
