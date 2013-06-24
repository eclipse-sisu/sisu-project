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

import org.eclipse.sisu.BeanScanning;
import org.eclipse.sisu.bean.DeclaredMembers;
import org.eclipse.sisu.bean.DeclaredMembers.View;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.IndexedClassFinder;

final class Extensions
{
    private final ClassSpace space;

    private final boolean global;

    private Extensions( final ClassSpace space, final boolean global )
    {
        this.space = space;
        this.global = global;
    }

    public static Extensions from( final ClassSpace space, final BeanScanning scanning )
    {
        return new Extensions( space, BeanScanning.GLOBAL_INDEX == scanning );
    }

    public <T> Iterable<T> of( final Class<T> api, final Object... args )
    {
        final List<T> instances = new ArrayList<T>();
        final String index = "META-INF/services/" + api.getName();
        for ( final String name : new IndexedClassFinder( index, global ).indexedNames( space ) )
        {
            try
            {
                @SuppressWarnings( "unchecked" )
                final Class<T> impl = (Class<T>) space.loadClass( name );
                if ( api.isAssignableFrom( impl ) )
                {
                    instances.add( newInstance( impl, args ) );
                }
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
        return instances;
    }

    @SuppressWarnings( "unchecked" )
    private static <T> T newInstance( final Class<T> impl, final Object... args )
        throws Exception
    {
        // filter out constructors with incompatible parameter types
        final Constructor<T>[] ctors = new Constructor[args.length + 1];
        for ( final Member m : new DeclaredMembers( impl, View.CONSTRUCTORS ) )
        {
            Constructor<T> ctor = (Constructor<T>) m;
            final Class<?>[] types = ctor.getParameterTypes();
            if ( types.length <= args.length )
            {
                for ( int i = 0; i < types.length; i++ )
                {
                    if ( !types[i].isInstance( args[i] ) )
                    {
                        ctor = null;
                        break;
                    }
                }
                if ( null != ctor )
                {
                    ctors[types.length] = ctor;
                }
            }
        }

        // perfect match
        if ( null != ctors[args.length] )
        {
            return ctors[args.length].newInstance( args );
        }

        // partial match
        for ( int i = args.length - 1; i > 0; i-- )
        {
            if ( null != ctors[i] )
            {
                final Object[] tempArgs = new Object[i];
                System.arraycopy( args, 0, tempArgs, 0, i );
                return ctors[i].newInstance( tempArgs );
            }
        }

        // last resort
        return impl.newInstance();
    }
}
