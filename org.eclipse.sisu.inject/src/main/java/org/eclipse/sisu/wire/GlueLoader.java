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
package org.eclipse.sisu.wire;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Provider;

import org.eclipse.sisu.inject.Weak;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

/**
 * Weak cache of {@link ClassLoader}s that can generate proxy classes on-demand.
 */
final class GlueLoader
    extends ClassLoader
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Object SYSTEM_LOADER_LOCK = new Object();

    private static final String PROVIDER_NAME = Provider.class.getName();

    private static final String GLUE_SUFFIX = "$__sisu__$";

    private static final String DYNAMIC = "dyn";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final ConcurrentMap<Integer, GlueLoader> cachedGlue = Weak.concurrentValues();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    GlueLoader()
    {
        // use system loader as parent
    }

    GlueLoader( final ClassLoader parent )
    {
        super( parent );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Generates a new dynamic proxy instance for the given facade type and provider.
     * 
     * @param type The facade type
     * @param provider The provider
     * @return Generated proxy instance
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T dynamicGlue( final TypeLiteral<T> type, final Provider<T> provider )
    {
        try
        {
            return (T) dynamicGlue( type.getRawType() ).getConstructor( Provider.class ).newInstance( provider );
        }
        catch ( final Exception e )
        {
            final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
            throw new ProvisionException( "Error proxying: " + type, cause );
        }
        catch ( final LinkageError e )
        {
            throw new ProvisionException( "Error proxying: " + type, e );
        }
    }

    // ----------------------------------------------------------------------
    // Class-loading methods
    // ----------------------------------------------------------------------

    @Override
    @SuppressWarnings( "sync-override" )
    protected Class<?> loadClass( final String name, final boolean resolve )
        throws ClassNotFoundException
    {
        // ensure our proxies have access to the following non-JDK types
        if ( PROVIDER_NAME.equals( name ) )
        {
            return Provider.class;
        }
        return super.loadClass( name, resolve );
    }

    @Override
    protected Class<?> findClass( final String name )
        throws ClassNotFoundException
    {
        if ( name.endsWith( GLUE_SUFFIX + DYNAMIC ) )
        {
            final Class<?> facade = loadClass( unwrap( name ) );
            final byte[] code = DynamicGlue.generateProxyClass( name.replace( '.', '/' ), facade );
            return defineClass( name, code, 0, code.length );
        }
        throw new ClassNotFoundException( name );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Loads the dynamic proxy class for the given facade class.
     */
    private static Class<?> dynamicGlue( final Class<?> facade )
        throws ClassNotFoundException
    {
        return glue( facade.getClassLoader() ).loadClass( wrap( facade.getName(), DYNAMIC ) );
    }

    /**
     * Wraps the given class name with the appropriate proxy decoration.
     */
    private static String wrap( final String name, final String kind )
    {
        final StringBuilder buf = new StringBuilder();
        if ( name.startsWith( "java." ) || name.startsWith( "java/" ) )
        {
            buf.append( '$' ); // proxy java.* types by changing the package space
        }
        return buf.append( name ).append( GLUE_SUFFIX ).append( kind ).toString();
    }

    /**
     * Unwraps the proxy decoration from around the given class name.
     */
    private static String unwrap( final String name )
    {
        final int head = '$' == name.charAt( 0 ) ? 1 : 0;
        final int tail = name.lastIndexOf( GLUE_SUFFIX );

        return tail > 0 ? name.substring( head, tail ) : name;
    }

    /**
     * Returns the {@link GlueLoader} associated with the given {@link ClassLoader}.
     */
    @SuppressWarnings( "boxing" )
    private static GlueLoader glue( final ClassLoader parent )
    {
        int id = System.identityHashCode( parent );

        GlueLoader result = cachedGlue.get( id );
        if ( null == result || result.getParent() != parent )
        {
            synchronized ( null != parent ? parent : SYSTEM_LOADER_LOCK )
            {
                final GlueLoader glue = createGlue( parent );
                do
                {
                    result = cachedGlue.putIfAbsent( id++, glue );
                    if ( null == result )
                    {
                        return glue;
                    }
                }
                while ( result.getParent() != parent );
            }
        }
        return result;
    }

    /**
     * Returns new {@link GlueLoader} that delegates to the given {@link ClassLoader}.
     */
    private static GlueLoader createGlue( final ClassLoader parent )
    {
        return AccessController.doPrivileged( new PrivilegedAction<GlueLoader>()
        {
            public GlueLoader run()
            {
                return null != parent ? new GlueLoader( parent ) : new GlueLoader();
            }
        } );
    }
}
