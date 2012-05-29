/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.locators;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.locators.spi.BindingSubscriber;
import org.eclipse.sisu.reflect.Logs;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Provides dynamic {@link BeanEntry} notifications by tracking qualified {@link Binding}s.
 * 
 * @see BeanLocator#watch(Key, Mediator, Object)
 */
final class WatchedBeans<Q extends Annotation, T, W>
    implements BindingSubscriber<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanCache<Q, T> beans = new BeanCache<Q, T>();

    private final Key<T> key;

    private final Mediator<Q, T, W> mediator;

    private final QualifyingStrategy strategy;

    private final Reference<W> watcherRef;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    WatchedBeans( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        this.key = key;
        this.mediator = mediator;

        strategy = QualifyingStrategy.selectFor( key );
        watcherRef = new WeakReference<W>( watcher );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public TypeLiteral<T> type()
    {
        return key.getTypeLiteral();
    }

    public void add( final Binding<T> binding, final int rank )
    {
        @SuppressWarnings( "unchecked" )
        final Q qualifier = (Q) strategy.qualifies( key, binding );
        if ( null != qualifier )
        {
            final W watcher = watcherRef.get();
            if ( null != watcher )
            {
                final BeanEntry<Q, T> bean = beans.create( qualifier, binding, rank );
                try
                {
                    mediator.add( bean, watcher );
                }
                catch ( final Throwable e )
                {
                    Logs.catchThrowable( e );
                    Logs.warn( "Problem adding: <> to: " + detail( watcher ), bean, e );
                }
            }
        }
    }

    public void remove( final Binding<T> binding )
    {
        final BeanEntry<Q, T> bean = beans.remove( binding );
        if ( null != bean )
        {
            final W watcher = watcherRef.get();
            if ( null != watcher )
            {
                try
                {
                    mediator.remove( bean, watcher );
                }
                catch ( final Throwable e )
                {
                    Logs.catchThrowable( e );
                    Logs.warn( "Problem removing: <> from: " + detail( watcher ), bean, e );
                }
            }
        }
    }

    public Iterable<Binding<T>> bindings()
    {
        return beans.bindings();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private String detail( final Object watcher )
    {
        return Logs.identityToString( watcher ) + " via: " + Logs.identityToString( mediator );
    }
}
