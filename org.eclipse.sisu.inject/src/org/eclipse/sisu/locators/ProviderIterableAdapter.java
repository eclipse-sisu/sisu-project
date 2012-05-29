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
import java.util.Iterator;
import java.util.Map.Entry;

import javax.inject.Provider;

import org.eclipse.sisu.BeanEntry;

/**
 * {@link Iterable} sequence of {@link Provider} entries backed by a sequence of {@link BeanEntry}s.
 */
public final class ProviderIterableAdapter<K extends Annotation, V>
    implements Iterable<Entry<K, Provider<V>>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<BeanEntry<K, V>> delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ProviderIterableAdapter( final Iterable<BeanEntry<K, V>> delegate )
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
