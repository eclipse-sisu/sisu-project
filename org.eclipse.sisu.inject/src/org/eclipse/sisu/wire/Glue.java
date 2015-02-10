/*******************************************************************************
 * Copyright (c) 2008, 2015 Stuart McCulloch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.wire;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Provider;

import org.eclipse.sisu.inject.Weak;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

final class Glue
    extends ClassLoader
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String UNAVAILABLE_CLAZZ_NAME = IllegalStateException.class.getName();

    private static final String PROVIDER_CLAZZ_NAME = Provider.class.getName();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final ConcurrentMap<Integer, Glue> cachedGlue = Weak.concurrentValues();

    private static final Integer[] loaderIdHolder = new Integer[1];

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    Glue()
    {
        // use system loader as parent
    }

    Glue( final ClassLoader parent )
    {
        super( parent );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public static <T> T dynamicInstance( final TypeLiteral<T> type, final Provider<T> provider )
    {
        try
        {
            return (T) getDynamicClass( type.getRawType() ).getConstructor( Provider.class ).newInstance( provider );
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
        // short-circuit access to these classes
        if ( PROVIDER_CLAZZ_NAME.equals( name ) )
        {
            return Provider.class;
        }
        if ( UNAVAILABLE_CLAZZ_NAME.equals( name ) )
        {
            return IllegalStateException.class;
        }

        return super.loadClass( name, resolve );
    }

    @Override
    protected Class<?> findClass( final String clazzOrProxyName )
        throws ClassNotFoundException
    {
        final String clazzName = DynamicGlue.getClazzName( clazzOrProxyName );

        // is this a new proxy class request?
        if ( !clazzName.equals( clazzOrProxyName ) )
        {
            final byte[] code = DynamicGlue.generateProxy( loadClass( clazzName ) );
            return defineClass( clazzOrProxyName, code, 0, code.length );
        }

        // ignore any non-proxy requests
        throw new ClassNotFoundException( clazzOrProxyName );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return unique proxy class per given type
     */
    private static Class<?> getDynamicClass( final Class<?> clazz )
        throws ClassNotFoundException
    {
        final ClassLoader parent = clazz.getClassLoader();

        Glue glue = fetchGlue( parent, null );
        if ( null == glue )
        {
            synchronized ( cachedGlue )
            {
                glue = fetchGlue( parent, loaderIdHolder );
                if ( null == glue )
                {
                    // still not cached, so go ahead with assigned id
                    cachedGlue.put( loaderIdHolder[0], glue = createGlue( parent ) );
                }
            }
        }

        return glue.loadClass( DynamicGlue.getProxyName( clazz.getName() ) );
    }

    @SuppressWarnings( "boxing" )
    private static Glue fetchGlue( final ClassLoader parent, final Integer[] idReturn )
    {
        // loader hash is nominally unique, but handle collisions just in case
        int id = System.identityHashCode( parent );

        Glue result;
        while ( null != ( result = cachedGlue.get( id ) ) && parent != result.getParent() )
        {
            id++; // collision! (should be very rare) ... resort to linear scan from base id
        }
        if ( null != idReturn )
        {
            idReturn[0] = id;
        }
        return result;
    }

    private static Glue createGlue( final ClassLoader parent )
    {
        return AccessController.doPrivileged( new PrivilegedAction<Glue>()
        {
            public Glue run()
            {
                return null != parent ? new Glue( parent ) : new Glue();
            }
        } );
    }
}
