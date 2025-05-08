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
package org.eclipse.sisu.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;

import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Provider;

/**
 * @deprecated Limited support for migrating legacy types.
 */
@Deprecated
public final class Legacy<S>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    @SuppressWarnings( "rawtypes" )
    private static final Legacy<org.eclipse.sisu.BeanEntry<?, ?>> LEGACY_BEAN_ENTRY =
        Legacy.<org.eclipse.sisu.BeanEntry<?, ?>, BeanEntry> as( BeanEntry.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Constructor<?> proxyConstructor;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Legacy( final Class<? extends S> clazz )
    {
        final Class<?> proxyClazz = Proxy.getProxyClass( clazz.getClassLoader(), clazz );
        try
        {
            this.proxyConstructor = proxyClazz.getConstructor( InvocationHandler.class );
        }
        catch ( final NoSuchMethodException e )
        {
            throw new IllegalStateException( e ); // should never occur
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public <T extends S> T proxy( final S delegate )
    {
        try
        {
            return null == delegate ? null : (T) proxyConstructor.newInstance( new InvocationHandler()
            {
                public Object invoke( final Object proxy, final Method method, final Object[] args )
                    throws Exception
                {
                    return method.invoke( delegate, args );
                }
            } );
        }
        catch ( final Exception e )
        {
            throw new IllegalStateException( e ); // should never occur
        }
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static <S, T extends S> Legacy<S> as( final Class<T> clazz )
    {
        return new Legacy<S>( clazz );
    }

    public static <Q extends Annotation, T> BeanEntry<Q, T> adapt( final org.eclipse.sisu.BeanEntry<Q, T> delegate )
    {
        return LEGACY_BEAN_ENTRY.proxy( delegate );
    }

    public static <Q extends Annotation, T> Iterable<BeanEntry<Q, T>> adapt( final Iterable<? extends org.eclipse.sisu.BeanEntry<Q, T>> delegate )
    {
        return new Iterable<BeanEntry<Q, T>>()
        {
            public Iterator<BeanEntry<Q, T>> iterator()
            {
                final Iterator<? extends org.eclipse.sisu.BeanEntry<Q, T>> itr = delegate.iterator();
                return new Iterator<BeanEntry<Q, T>>()
                {
                    public boolean hasNext()
                    {
                        return itr.hasNext();
                    }

                    public BeanEntry<Q, T> next()
                    {
                        return Legacy.adapt( itr.next() );
                    }

                    public void remove()
                    {
                        itr.remove();
                    }
                };
            }
        };
    }

    public static <Q extends Annotation, T> Provider<Iterable<BeanEntry<Q, T>>> adapt( final Provider<Iterable<? extends org.eclipse.sisu.BeanEntry<Q, T>>> delegate )
    {
        return new Provider<Iterable<BeanEntry<Q, T>>>()
        {
            public Iterable<BeanEntry<Q, T>> get()
            {
                return Legacy.adapt( delegate.get() );
            }
        };
    }

    public static <Q extends Annotation, T, W> org.eclipse.sisu.Mediator<Q, T, W> adapt( final Mediator<Q, T, W> delegate )
    {
        return null == delegate ? null : new org.eclipse.sisu.Mediator<Q, T, W>()
        {
            public void add( final org.eclipse.sisu.BeanEntry<Q, T> entry, final W watcher )
                throws Exception
            {
                delegate.add( Legacy.adapt( entry ), watcher );
            }

            public void remove( final org.eclipse.sisu.BeanEntry<Q, T> entry, final W watcher )
                throws Exception
            {
                delegate.remove( Legacy.adapt( entry ), watcher );
            }
        };
    }
}
