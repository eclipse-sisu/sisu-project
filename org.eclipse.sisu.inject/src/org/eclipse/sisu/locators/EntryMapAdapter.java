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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * {@link Map} backed by an {@link Iterable} sequence of map entries.
 */
public final class EntryMapAdapter<K, V>
    extends AbstractMap<K, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Entry<K, V>> entrySet;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public EntryMapAdapter( final Iterable<? extends Entry<K, V>> iterable )
    {
        entrySet = new EntrySet<K, V>( iterable );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return entrySet;
    }

    @Override
    public boolean isEmpty()
    {
        return entrySet.isEmpty();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Entry {@link Set} backed by an {@link Iterable} sequence of map entries.
     */
    private static final class EntrySet<K, V>
        extends AbstractSet<Entry<K, V>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterable<Entry<K, V>> iterable;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        EntrySet( final Iterable<? extends Entry<K, V>> iterable )
        {
            this.iterable = (Iterable<Entry<K, V>>) iterable;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public Iterator<Entry<K, V>> iterator()
        {
            return iterable.iterator();
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
    }
}
