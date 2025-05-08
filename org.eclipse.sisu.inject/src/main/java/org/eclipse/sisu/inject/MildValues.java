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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * NON-thread-safe {@link Map} whose values are kept alive by soft/weak {@link Reference}s.
 */
class MildValues<K, V>
    implements Map<K, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final ReferenceQueue<V> queue = new ReferenceQueue<V>();

    final Map<K, Reference<V>> map;

    private final boolean soft;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MildValues( final Map<K, Reference<V>> map, final boolean soft )
    {
        this.map = map;
        this.soft = soft;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final boolean containsKey( final Object key )
    {
        // skip compact for performance reasons

        return map.containsKey( key );
    }

    public final boolean containsValue( final Object value )
    {
        // skip compact for performance reasons

        return map.containsValue( tempValue( value ) );
    }

    public final V get( final Object key )
    {
        // skip compact for performance reasons

        final Reference<V> ref = map.get( key );
        return null != ref ? ref.get() : null;
    }

    public final V put( final K key, final V value )
    {
        compact();

        final Reference<V> ref = map.put( key, mildValue( key, value ) );
        return null != ref ? ref.get() : null;
    }

    public final void putAll( final Map<? extends K, ? extends V> m )
    {
        compact();

        for ( final Entry<? extends K, ? extends V> e : m.entrySet() )
        {
            map.put( e.getKey(), mildValue( e.getKey(), e.getValue() ) );
        }
    }

    public final V remove( final Object key )
    {
        compact();

        final Reference<V> ref = map.remove( key );
        return null != ref ? ref.get() : null;
    }

    public final void clear()
    {
        map.clear();

        compact();
    }

    public final boolean isEmpty()
    {
        compact();

        return map.isEmpty();
    }

    public final int size()
    {
        compact();

        return map.size();
    }

    public final Set<K> keySet()
    {
        compact();

        return map.keySet();
    }

    public final Collection<V> values()
    {
        compact();

        return new AbstractCollection<V>()
        {
            @Override
            public Iterator<V> iterator()
            {
                return new ValueItr();
            }

            @Override
            public int size()
            {
                return map.size();
            }
        };
    }

    public final Set<Entry<K, V>> entrySet()
    {
        compact();

        return new AbstractSet<Entry<K, V>>()
        {
            @Override
            public Iterator<Entry<K, V>> iterator()
            {
                return new EntryItr();
            }

            @Override
            public int size()
            {
                return map.size();
            }
        };
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Soft or weak {@link Reference} for the given key-value mapping.
     */
    final Reference<V> mildValue( final K key, final V value )
    {
        return soft ? new Soft<K, V>( key, value, queue ) : new Weak<K, V>( key, value, queue );
    }

    /**
     * @return Temporary {@link Reference} for the given value; used in queries.
     */
    static final <V> Reference<V> tempValue( final V value )
    {
        return new Weak<V, V>( null, value, null );
    }

    /**
     * Compacts the map by removing cleared values.
     */
    void compact()
    {
        for ( Reference<? extends V> ref; ( ref = queue.poll() ) != null; )
        {
            // only remove this specific key-value mapping
            final Object key = ( (InverseMapping) ref ).key();
            if ( map.get( key ) == ref )
            {
                map.remove( key );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Represents an inverse mapping from a value to its key.
     */
    interface InverseMapping
    {
        Object key();
    }

    /**
     * Soft value with an {@link InverseMapping} back to its key.
     */
    private static final class Soft<K, V>
        extends MildKeys.Soft<V>
        implements InverseMapping
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final K key;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Soft( final K key, final V value, final ReferenceQueue<V> queue )
        {
            super( value, queue );
            this.key = key;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public Object key()
        {
            return key;
        }
    }

    /**
     * Weak value with an {@link InverseMapping} back to its key.
     */
    private static final class Weak<K, V>
        extends MildKeys.Weak<V>
        implements InverseMapping
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final K key;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Weak( final K key, final V value, final ReferenceQueue<V> queue )
        {
            super( value, queue );
            this.key = key;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public Object key()
        {
            return key;
        }
    }

    /**
     * {@link Iterator} that iterates over reachable values in the map.
     */
    final class ValueItr
        implements Iterator<V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Reference<V>> itr = map.values().iterator();

        private V nextValue;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            // find next value that is still reachable
            while ( null == nextValue && itr.hasNext() )
            {
                nextValue = itr.next().get();
            }
            return null != nextValue;
        }

        public V next()
        {
            if ( hasNext() )
            {
                // populated by hasNext()
                final V value = nextValue;
                nextValue = null;
                return value;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            itr.remove();
        }
    }

    /**
     * {@link Iterator} that iterates over reachable entries in the map.
     */
    final class EntryItr
        implements Iterator<Entry<K, V>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Entry<K, Reference<V>>> itr = map.entrySet().iterator();

        private Entry<K, Reference<V>> nextEntry;

        private V nextValue;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            // find next entry that is still reachable
            while ( null == nextValue && itr.hasNext() )
            {
                nextEntry = itr.next();
                nextValue = nextEntry.getValue().get();
            }
            return null != nextValue;
        }

        public Entry<K, V> next()
        {
            if ( hasNext() )
            {
                // populated by hasNext()
                final Entry<K, V> entry = new StrongEntry( nextEntry, nextValue );
                nextEntry = null;
                nextValue = null;
                return entry;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            itr.remove();
        }
    }

    /**
     * {@link Entry} that delegates to the original entry, but maintains a strong reference to the value.
     */
    final class StrongEntry
        implements Entry<K, V>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Entry<K, Reference<V>> entry;

        private V value;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        StrongEntry( final Entry<K, Reference<V>> entry, final V value )
        {
            this.entry = entry;
            this.value = value;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public K getKey()
        {
            return entry.getKey();
        }

        public V getValue()
        {
            return value;
        }

        public V setValue( final V newValue )
        {
            final V oldValue = value;
            entry.setValue( mildValue( getKey(), newValue ) );
            value = newValue;
            return oldValue;
        }
    }
}
