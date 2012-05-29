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

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * {@link List} backed by an {@link Iterable} sequence of map entries.
 */
public final class EntryListAdapter<K, V>
    extends AbstractSequentialList<V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<? extends Entry<K, V>> iterable;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public EntryListAdapter( final Iterable<? extends Entry<K, V>> iterable )
    {
        this.iterable = iterable;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Iterator<V> iterator()
    {
        return new ValueIterator<K, V>( iterable );
    }

    @Override
    public ListIterator<V> listIterator( final int index )
    {
        return new ValueListIterator<K, V>( iterable, index );
    }

    @Override
    public boolean isEmpty()
    {
        return false == iterator().hasNext();
    }

    @Override
    public int size()
    {
        int size = 0;
        for ( final Iterator<?> i = iterable.iterator(); i.hasNext(); i.next() )
        {
            size++;
        }
        return size;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Value {@link Iterator} backed by a Key:Value {@link Iterator}.
     */
    private static final class ValueIterator<K, V>
        implements Iterator<V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<? extends Entry<K, V>> iterator;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ValueIterator( final Iterable<? extends Entry<K, V>> iterable )
        {
            this.iterator = iterable.iterator();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        public V next()
        {
            return iterator.next().getValue();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Value {@link ListIterator} backed by a cached Key:Value {@link Iterator}.
     */
    private static final class ValueListIterator<K, V>
        implements ListIterator<V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<? extends Entry<K, V>> iterator;

        private final List<Entry<K, V>> entryCache = new ArrayList<Entry<K, V>>();

        private int index;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ValueListIterator( final Iterable<? extends Entry<K, V>> iterable, final int index )
        {
            if ( index < 0 )
            {
                throw new IndexOutOfBoundsException();
            }
            this.iterator = iterable.iterator();
            try
            {
                while ( this.index < index )
                {
                    next(); // position iterator at the index position
                }
            }
            catch ( final NoSuchElementException e )
            {
                throw new IndexOutOfBoundsException();
            }
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            return index < entryCache.size() || iterator.hasNext();
        }

        public boolean hasPrevious()
        {
            return index > 0;
        }

        public V next()
        {
            if ( index >= entryCache.size() )
            {
                entryCache.add( iterator.next() );
            }
            return entryCache.get( index++ ).getValue();
        }

        public V previous()
        {
            if ( index <= 0 )
            {
                throw new NoSuchElementException();
            }
            return entryCache.get( --index ).getValue();
        }

        public int nextIndex()
        {
            return index;
        }

        public int previousIndex()
        {
            return index - 1;
        }

        public void add( final V o )
        {
            throw new UnsupportedOperationException();
        }

        public void set( final V o )
        {
            throw new UnsupportedOperationException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
