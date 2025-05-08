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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Delegating {@link Map} that merges a series of {@link Map}s into one consistent view.
 */
final class MergedProperties
    extends AbstractMap<Object, Object>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private transient volatile Set<Entry<Object, Object>> entrySet; // NOSONAR

    final Map<?, ?>[] properties;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MergedProperties( final List<Map<?, ?>> properties )
    {
        this.properties = properties.toArray( new Map[properties.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Object get( final Object key )
    {
        for ( final Map<?, ?> p : properties )
        {
            final Object value = p.get( key );
            if ( null != value )
            {
                return value;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey( final Object key )
    {
        for ( final Map<?, ?> p : properties )
        {
            if ( p.containsKey( key ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<Entry<Object, Object>> entrySet()
    {
        if ( null == entrySet )
        {
            entrySet = new MergedEntries();
        }
        return entrySet;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    final class MergedEntries
        extends AbstractSet<Entry<Object, Object>>
    {
        @Override
        public Iterator<Entry<Object, Object>> iterator()
        {
            return new Iterator<Entry<Object, Object>>()
            {
                @SuppressWarnings( "rawtypes" )
                private Iterator<? extends Entry> itr;

                private int index;

                public boolean hasNext()
                {
                    while ( null == itr || !itr.hasNext() )
                    {
                        if ( index >= properties.length )
                        {
                            return false;
                        }
                        itr = properties[index++].entrySet().iterator();
                    }
                    return true;
                }

                @SuppressWarnings( "unchecked" )
                public Entry<Object, Object> next()
                {
                    if ( hasNext() )
                    {
                        return itr.next();
                    }
                    throw new NoSuchElementException();
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size()
        {
            int size = 0;
            for ( final Map<?, ?> p : properties )
            {
                size += p.size();
            }
            return size;
        }
    }
}
