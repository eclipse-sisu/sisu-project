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
package org.eclipse.sisu.wire;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.TypeArguments;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

/**
 * Supplies various bean {@link Provider}s backed by dynamic bean lookups.
 */
final class BeanProviders
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Provider<BeanLocator> locator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanProviders( final Binder binder )
    {
        locator = binder.getProvider( BeanLocator.class );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Provides {@link Iterable} sequences of raw {@link BeanEntry}s.
     */
    public <K extends Annotation, V> Provider<Iterable<? extends BeanEntry<K, V>>> beanEntriesOf( final Key<V> key )
    {
        return new Provider<Iterable<? extends BeanEntry<K, V>>>()
        {
            public Iterable<? extends BeanEntry<K, V>> get()
            {
                return locator.get().locate( key );
            }
        };
    }

    /**
     * Provides {@link Iterable} sequences of bean/provider mappings
     */
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public <K extends Annotation, V> Provider<Iterable<Entry<K, V>>> entriesOf( final Key key )
    {
        final TypeLiteral<V> type = key.getTypeLiteral();
        final Class<?> clazz = type.getRawType();
        if ( javax.inject.Provider.class != clazz && com.google.inject.Provider.class != clazz )
        {
            return beanEntriesOf( key );
        }
        final Provider<Iterable<BeanEntry>> beanEntries = beanEntriesOf( key.ofType( TypeArguments.get( type, 0 ) ) );
        return new Provider<Iterable<Entry<K, V>>>()
        {
            public Iterable<Entry<K, V>> get()
            {
                return new ProviderIterableAdapter( beanEntries.get() );
            }
        };
    }

    /**
     * Provides {@link List}s of qualified beans/providers.
     */
    public <K extends Annotation, V> Provider<List<V>> listOf( final Key<V> key )
    {
        final Provider<Iterable<Entry<K, V>>> entries = entriesOf( key );
        return new Provider<List<V>>()
        {
            public List<V> get()
            {
                return new EntryListAdapter<V>( entries.get() );
            }
        };
    }

    /**
     * Provides {@link Set}s of qualified beans/providers.
     */
    public <K extends Annotation, V> Provider<Set<V>> setOf( final Key<V> key )
    {
        final Provider<Iterable<Entry<K, V>>> entries = entriesOf( key );
        return new Provider<Set<V>>()
        {
            public Set<V> get()
            {
                return new EntrySetAdapter<V>( entries.get() );
            }
        };
    }

    /**
     * Provides {@link Map}s of qualified beans/providers.
     */
    public <K extends Annotation, V> Provider<Map<K, V>> mapOf( final Key<V> key )
    {
        final Provider<Iterable<Entry<K, V>>> entries = entriesOf( key );
        return new Provider<Map<K, V>>()
        {
            public Map<K, V> get()
            {
                return new EntryMapAdapter<K, V>( entries.get() );
            }
        };
    }

    /**
     * Provides string {@link Map}s of named beans/providers.
     */
    public <V> Provider<Map<String, V>> stringMapOf( final TypeLiteral<V> type )
    {
        final Provider<Iterable<Entry<Named, V>>> entries = entriesOf( Key.get( type, Named.class ) );
        return new Provider<Map<String, V>>()
        {
            public Map<String, V> get()
            {
                return new EntryMapAdapter<String, V>( new NamedIterableAdapter<V>( entries.get() ) );
            }
        };
    }

    /**
     * Provides single qualified beans/providers.
     */
    <V> Provider<V> firstOf( final Key<V> key )
    {
        final Provider<Iterable<? extends BeanEntry<Annotation, V>>> beanEntries = beanEntriesOf( key );
        return new Provider<V>()
        {
            private volatile Iterable<? extends BeanEntry<?, V>> cachedLookup; // NOSONAR

            public V get()
            {
                if ( null == cachedLookup )
                {
                    cachedLookup = beanEntries.get();
                }
                final Iterator<? extends BeanEntry<?, V>> itr = cachedLookup.iterator();
                return itr.hasNext() ? itr.next().getProvider().get() : null;
            }
        };
    }

    /**
     * Provides placeholder beans/providers.
     */
    public <V> Provider<V> placeholderOf( final Key<V> key )
    {
        return new PlaceholderBeanProvider<V>( this, key );
    }
}
