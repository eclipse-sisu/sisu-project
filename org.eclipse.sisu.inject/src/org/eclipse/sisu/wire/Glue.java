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

    private static final String PROVIDER_NAME = Provider.class.getName();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final ConcurrentMap<Integer, Glue> cachedGlue = Weak.concurrentValues();

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
        // ensure our proxies have access to the following non-JDK types
        if ( PROVIDER_NAME.equals( name ) )
        {
            return Provider.class;
        }
        return super.loadClass( name, resolve );
    }

    @Override
    protected Class<?> findClass( final String clazzOrProxyName )
        throws ClassNotFoundException
    {
        if ( DynamicGlue.isProxyRequest( clazzOrProxyName ) )
        {
            final String clazzName = DynamicGlue.getClazzName( clazzOrProxyName );
            final byte[] code = DynamicGlue.generateProxy( loadClass( clazzName ) );
            return defineClass( clazzOrProxyName, code, 0, code.length );
        }
        throw new ClassNotFoundException( clazzOrProxyName );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static Class<?> getDynamicClass( final Class<?> clazz )
        throws ClassNotFoundException
    {
        return glue( clazz.getClassLoader() ).loadClass( DynamicGlue.getProxyName( clazz.getName() ) );
    }

    @SuppressWarnings( "boxing" )
    private static Glue glue( final ClassLoader parent )
    {
        int id = System.identityHashCode( parent );

        Glue result = cachedGlue.get( id );
        if ( null == result || result.getParent() != parent )
        {
            synchronized ( parent )
            {
                final Glue glue = createGlue( parent );
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
