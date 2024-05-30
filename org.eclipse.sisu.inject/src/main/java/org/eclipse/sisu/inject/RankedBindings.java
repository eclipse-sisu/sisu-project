/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
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
import java.util.Collection;
import java.util.Iterator;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;

/**
 * Ordered sequence of {@link Binding}s of a given type; subscribes to {@link BindingPublisher}s on demand.
 */
final class RankedBindings<T>
    implements Iterable<Binding<T>>, BindingSubscriber<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final transient RankedSequence<Binding<T>> bindings = new RankedSequence<Binding<T>>();

    final transient TypeLiteral<T> type;

    final transient RankedSequence<BindingPublisher> pendingPublishers;

    final Collection<BeanCache<?, T>> cachedBeans = Weak.elements();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RankedBindings( final TypeLiteral<T> type, final RankedSequence<BindingPublisher> publishers )
    {
        this.type = type;
        this.pendingPublishers = new RankedSequence<BindingPublisher>( publishers );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public TypeLiteral<T> type()
    {
        return type;
    }

    public void add( final Binding<T> binding, final int rank )
    {
        bindings.insert( binding, rank );
    }

    public void remove( final Binding<T> binding )
    {
        if ( bindings.removeThis( binding ) )
        {
            synchronized ( cachedBeans )
            {
                for ( final BeanCache<?, T> beans : cachedBeans )
                {
                    beans.remove( binding );
                }
            }
        }
    }

    public Iterable<Binding<T>> bindings()
    {
        return bindings.snapshot();
    }

    public Itr iterator()
    {
        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    <Q extends Annotation> BeanCache<Q, T> newBeanCache()
    {
        final BeanCache<Q, T> beans = new BeanCache<Q, T>();
        synchronized ( cachedBeans )
        {
            cachedBeans.add( beans );
        }
        return beans;
    }

    void add( final BindingPublisher publisher, final int rank )
    {
        /*
         * No need to lock; ranked sequence is thread-safe.
         */
        pendingPublishers.insert( publisher, rank );
    }

    void remove( final BindingPublisher publisher )
    {
        /*
         * Lock just to prevent subscription race condition.
         */
        synchronized ( publisher ) // NOSONAR
        {
            if ( !pendingPublishers.removeThis( publisher ) )
            {
                publisher.unsubscribe( this );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link Binding} iterator that only subscribes to {@link BindingPublisher}s as required.
     */
    final class Itr
        implements Iterator<Binding<T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final RankedSequence<Binding<T>>.Itr itr = bindings.iterator();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            // apply any publishers that could add bindings before the current position
            BindingPublisher publisher = pendingPublishers.peek();
            while ( null != publisher && !itr.hasNext( publisher.maxBindingRank() ) )
            {
                synchronized ( publisher )
                {
                    // check in case subscribed by another thread
                    if ( publisher == pendingPublishers.peek() )
                    {
                        // only update list _after_ subscription
                        publisher.subscribe( RankedBindings.this );
                        pendingPublishers.removeThis( publisher );
                    }
                }
                publisher = pendingPublishers.peek();
            }
            return itr.hasNext();
        }

        public Binding<T> next()
        {
            return itr.next();
        }

        public int rank()
        {
            return itr.rank();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
