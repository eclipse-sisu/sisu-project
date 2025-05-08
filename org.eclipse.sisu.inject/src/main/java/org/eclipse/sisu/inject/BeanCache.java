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
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.eclipse.sisu.BeanEntry;

import com.google.inject.Binding;

/**
 * Atomic cache mapping {@link Binding}s to {@link BeanEntry}s; optimized for common case of single entries.
 * <p>
 * Uses {@code ==} instead of {@code equals} to compare {@link Binding}s because we want referential equality.
 */
@SuppressWarnings( { "rawtypes", "unchecked" } )
final class BeanCache<Q extends Annotation, T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    private static final AtomicReferenceFieldUpdater<BeanCache, Object> MAPPING_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater( BeanCache.class, Object.class, "mapping" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private volatile Object mapping; // NOSONAR

    private Map<Binding<T>, BeanEntry<Q, T>> readCache;

    private volatile boolean mutated;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Atomically creates a new {@link BeanEntry} for the given {@link Binding} reference.
     * 
     * @param qualifier The qualifier
     * @param binding The binding
     * @param rank The assigned rank
     * @return Associated bean entry
     */
    public BeanEntry<Q, T> create( final Q qualifier, final Binding<T> binding, final int rank )
    {
        LazyBeanEntry newBean;

        Object o, n;

        /*
         * Compare-and-swap approach; avoids locking without missing any updates
         */
        do
        {
            o = mapping;
            if ( null == o )
            {
                // most common case: adding the one (and-only) entry
                n = newBean = new LazyBeanEntry( qualifier, binding, rank );
            }
            else if ( o instanceof LazyBeanEntry )
            {
                final LazyBeanEntry oldBean = (LazyBeanEntry) o;
                if ( binding == oldBean.binding )
                {
                    return oldBean;
                }
                n = createMap( oldBean, newBean = new LazyBeanEntry( qualifier, binding, rank ) );
            }
            else
            {
                synchronized ( this )
                {
                    final Map<Binding, LazyBeanEntry> map = (Map) o;
                    if ( null == ( newBean = map.get( binding ) ) )
                    {
                        map.put( binding, newBean = new LazyBeanEntry( qualifier, binding, rank ) );
                        mutated = true;
                    }
                    return newBean;
                }
            }
        }
        while ( !MAPPING_UPDATER.compareAndSet( this, o, n ) );

        if ( n instanceof IdentityHashMap )
        {
            mutated = true; // entry was upgraded to map, enable readCache
        }

        return newBean;
    }

    /**
     * @return Read-only snapshot of the cache
     */
    public Map<Binding<T>, BeanEntry<Q, T>> flush()
    {
        if ( mutated )
        {
            synchronized ( this )
            {
                if ( mutated )
                {
                    readCache = (Map) ( (IdentityHashMap) mapping ).clone();
                    mutated = false;
                }
            }
        }
        return readCache; // NOSONAR see 'happens-before' condition above
    }

    /**
     * Retrieves the {@link Binding} references currently associated with {@link BeanEntry}s.
     * 
     * @return Associated bindings
     */
    public Iterable<Binding<T>> bindings()
    {
        final Object o = mapping;
        if ( null == o )
        {
            return Collections.EMPTY_SET;
        }
        else if ( o instanceof LazyBeanEntry )
        {
            return Collections.singleton( ( (LazyBeanEntry<?, T>) o ).binding );
        }
        synchronized ( this )
        {
            return new ArrayList( ( (Map<Binding, BeanEntry>) o ).keySet() );
        }
    }

    /**
     * Removes the {@link BeanEntry} associated with the given {@link Binding} reference.
     * 
     * @param binding The binding
     * @return Associated bean entry
     */
    public BeanEntry<Q, T> remove( final Binding<T> binding )
    {
        LazyBeanEntry oldBean;

        Object o, n;

        /*
         * Compare-and-swap approach; avoids locking without missing any updates
         */
        do
        {
            o = mapping;
            if ( null == o )
            {
                return null;
            }
            else if ( o instanceof LazyBeanEntry )
            {
                oldBean = (LazyBeanEntry) o;
                if ( binding != oldBean.binding )
                {
                    return null;
                }
                n = null; // clear single entry
            }
            else
            {
                synchronized ( this )
                {
                    oldBean = ( (Map<?, LazyBeanEntry>) o ).remove( binding );
                    if ( null != oldBean )
                    {
                        mutated = true;
                    }
                    return oldBean;
                }
            }
        }
        while ( !MAPPING_UPDATER.compareAndSet( this, o, n ) );

        return oldBean;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static Map createMap( final LazyBeanEntry one, final LazyBeanEntry two )
    {
        final Map map = new IdentityHashMap( 10 );
        map.put( one.binding, one );
        map.put( two.binding, two );
        return map;
    }
}
