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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Provider;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

final class GlueCache
    extends ClassLoader
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String UNAVAILABLE_CLAZZ_NAME = IllegalStateException.class.getName();

    private static final String PROVIDER_CLAZZ_NAME = Provider.class.getName();

    private static final Object NULL_CLASS_LOADER_KEY = new Object();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    // FIXME: should be weak map of classloaders, to allow eager collection of proxied classes
    private static final ConcurrentMap<Object, ClassLoader> LOADER_MAP = new ConcurrentHashMap<Object, ClassLoader>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    // delegate to the original type's classloader
    GlueCache( final ClassLoader parent )
    {
        super( parent );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public static <T> T glue( final TypeLiteral<T> type, final Provider<T> provider )
    {
        try
        {
            return (T) getProxyClass( type.getRawType() ).getConstructor( Provider.class ).newInstance( provider );
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
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return unique proxy class per given type
     */
    static Class<?> getProxyClass( final Class<?> clazz )
        throws ClassNotFoundException
    {
        final Object key = getKeyFromClassLoader( clazz.getClassLoader() );
        final String name = ProviderGlue.getProxyName( clazz.getName() );

        ClassLoader loader = LOADER_MAP.get( key );
        if ( null == loader )
        {
            synchronized ( LOADER_MAP )
            {
                if ( null == ( loader = LOADER_MAP.get( key ) ) )
                {
                    LOADER_MAP.put( key, loader = new GlueCache( getClassLoaderFromKey( key ) ) );
                }
            }
        }
        return loader.loadClass( name );
    }

    /**
     * @return non-null key for the given class loader
     */
    static Object getKeyFromClassLoader( final ClassLoader classLoader )
    {
        if ( null != classLoader )
        {
            return classLoader;
        }

        try
        {
            return AccessController.doPrivileged( new PrivilegedAction<ClassLoader>()
            {
                public ClassLoader run()
                {
                    return getSystemClassLoader();
                }
            } );
        }
        catch ( final SecurityException e )
        {
            return NULL_CLASS_LOADER_KEY; // unable to canonicalise!
        }
    }

    /**
     * @return class loader related to the given key
     */
    static ClassLoader getClassLoaderFromKey( final Object key )
    {
        return NULL_CLASS_LOADER_KEY != key ? (ClassLoader) key : null; // NOPMD
    }

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
        final String clazzName = ProviderGlue.getClazzName( clazzOrProxyName );

        // is this a new proxy class request?
        if ( !clazzName.equals( clazzOrProxyName ) )
        {
            final byte[] code = ProviderGlue.generateProxy( loadClass( clazzName ) );
            return defineClass( clazzOrProxyName, code, 0, code.length );
        }

        // ignore any non-proxy requests
        throw new ClassNotFoundException( clazzOrProxyName );
    }
}
