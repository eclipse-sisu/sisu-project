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

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.inject.Provider;

import org.eclipse.sisu.BeanEntry;

/**
 * {@link Iterable} sequence of {@link Provider} entries backed by a sequence of {@link BeanEntry}s.
 */
final class ProviderIterableAdapter<K extends Annotation, V>
    implements Iterable<Entry<K, Provider<V>>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<BeanEntry<K, V>> delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ProviderIterableAdapter( final Iterable<BeanEntry<K, V>> delegate )
    {
        this.delegate = delegate;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<Entry<K, Provider<V>>> iterator()
    {
        return new ProviderIterator<K, V>( delegate );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Iterator of {@link Provider} {@link Entry}s backed by an iterator of {@link BeanEntry}s.
     */
    private static final class ProviderIterator<K extends Annotation, V>
        implements Iterator<Entry<K, Provider<V>>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<BeanEntry<K, V>> iterator;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ProviderIterator( final Iterable<BeanEntry<K, V>> iterable )
        {
            iterator = iterable.iterator();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        public Entry<K, Provider<V>> next()
        {
            return new ProviderEntry<K, V>( iterator.next() );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@link Provider} {@link Entry} backed by a {@link BeanEntry}.
     */
    private static final class ProviderEntry<K extends Annotation, V>
        implements Entry<K, Provider<V>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final BeanEntry<K, V> entry;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ProviderEntry( final BeanEntry<K, V> entry )
        {
            this.entry = entry;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public K getKey()
        {
            return entry.getKey();
        }

        public Provider<V> getValue()
        {
            return entry.getProvider();
        }

        public Provider<V> setValue( final Provider<V> value )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString()
        {
            return getKey() + "=" + getValue();
        }
    }
}
