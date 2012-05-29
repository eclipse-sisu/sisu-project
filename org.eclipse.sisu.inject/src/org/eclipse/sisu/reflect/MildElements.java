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
package org.eclipse.sisu.reflect;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * NON-thread-safe {@link Collection} of elements kept alive by soft/weak {@link Reference}s.
 */
final class MildElements<T>
    extends AbstractCollection<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ReferenceQueue<T> queue = new ReferenceQueue<T>();

    final List<Reference<T>> list;

    private final boolean soft;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MildElements( final List<Reference<T>> list, final boolean soft )
    {
        this.list = list;
        this.soft = soft;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public boolean add( final T element )
    {
        compact();

        return list.add( soft ? new Soft<T>( element, queue, list.size() ) : new Weak<T>( element, queue, list.size() ) );
    }

    @Override
    public int size()
    {
        compact();

        return list.size();
    }

    @Override
    public Iterator<T> iterator()
    {
        compact();

        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Compacts the list by replacing unreachable {@link Reference}s with ones from the end.
     */
    private void compact()
    {
        Reference<? extends T> ref;
        while ( ( ref = queue.poll() ) != null )
        {
            evict( ref );
        }
    }

    /**
     * Evicts a single {@link Reference} from the list; replacing it with one from the end.
     * 
     * @param ref The reference to evict
     */
    void evict( final Reference<? extends T> ref )
    {
        final int index = ( (Indexable) ref ).index( -1 );
        if ( index >= 0 )
        {
            final Reference<T> last = list.remove( list.size() - 1 );
            if ( ref != last )
            {
                ( (Indexable) last ).index( index );
                list.set( index, last );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Represents an element that can be indexed.
     */
    private interface Indexable
    {
        int index( int index );
    }

    /**
     * {@link Iterator} that iterates over reachable {@link Reference}s in the list.
     */
    final class Itr
        implements Iterator<T>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private int index;

        private T nextElement;

        private boolean haveElement;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            // find next element that is still reachable
            while ( null == nextElement && index < list.size() )
            {
                nextElement = list.get( index++ ).get();
            }
            return null != nextElement;
        }

        public T next()
        {
            haveElement = hasNext();
            if ( haveElement )
            {
                // populated by hasNext()
                final T element = nextElement;
                nextElement = null;
                return element;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            if ( haveElement )
            {
                evict( list.get( --index ) );
                haveElement = false;
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Soft {@link Indexable} element.
     */
    private static final class Soft<T>
        extends SoftReference<T>
        implements Indexable
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private int index;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Soft( final T value, final ReferenceQueue<T> queue, final int index )
        {
            super( value, queue );
            this.index = index;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public int index( final int newIndex )
        {
            final int oldIndex = index;
            index = newIndex;
            return oldIndex;
        }
    }

    /**
     * Weak {@link Indexable} element.
     */
    private static final class Weak<T>
        extends WeakReference<T>
        implements Indexable
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private int index;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Weak( final T value, final ReferenceQueue<T> queue, final int index )
        {
            super( value, queue );
            this.index = index;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public int index( final int newIndex )
        {
            final int oldIndex = index;
            index = newIndex;
            return oldIndex;
        }
    }
}
