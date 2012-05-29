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

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.inject.name.Named;

/**
 * String mapping {@link Iterable} backed by a {@link Named} mapping {@link Iterable}.
 */
public final class NamedIterableAdapter<V>
    implements Iterable<Entry<String, V>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Entry<Named, V>> delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public NamedIterableAdapter( final Iterable<Entry<Named, V>> delegate )
    {
        this.delegate = delegate;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<Entry<String, V>> iterator()
    {
        return new NamedIterator<V>( delegate );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * String mapping {@link Iterator} backed by a {@link Named} mapping {@link Iterator}.
     */
    private static final class NamedIterator<V>
        implements Iterator<Entry<String, V>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Entry<Named, V>> iterator;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        NamedIterator( final Iterable<Entry<Named, V>> iterable )
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

        public Entry<String, V> next()
        {
            return new NamedEntry<V>( iterator.next() );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * String mapping {@link Entry} backed by a {@link Named} mapping {@link Entry}.
     */
    private static final class NamedEntry<V>
        implements Entry<String, V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Entry<Named, V> entry;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        NamedEntry( final Entry<Named, V> entry )
        {
            this.entry = entry;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public String getKey()
        {
            return entry.getKey().value();
        }

        public V getValue()
        {
            return entry.getValue();
        }

        public V setValue( final V value )
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
