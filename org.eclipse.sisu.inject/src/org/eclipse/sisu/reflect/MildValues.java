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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

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

    private final Map<K, Reference<V>> map;

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

    /**
     * WARNING: this view is a snapshot; updates to it are <em>not</em> reflected in the original map, or vice-versa.
     * <hr>
     * {@inheritDoc}
     */
    public final Collection<V> values()
    {
        compact();

        final List<V> values = new ArrayList<V>();
        for ( final Reference<V> r : map.values() )
        {
            final V value = r.get();
            if ( null != value )
            {
                values.add( value );
            }
        }
        return values;
    }

    /**
     * WARNING: this view is a snapshot; updates to it are <em>not</em> reflected in the original map, or vice-versa.
     * <hr>
     * {@inheritDoc}
     */
    public final Set<Entry<K, V>> entrySet()
    {
        compact();

        final Map<K, V> entries = new HashMap<K, V>();
        for ( final Entry<K, Reference<V>> e : map.entrySet() )
        {
            final V value = e.getValue().get();
            if ( null != value )
            {
                entries.put( e.getKey(), value );
            }
        }
        return entries.entrySet();
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
    static final Reference<?> tempValue( final Object value )
    {
        return new Weak<Object, Object>( null, value, null );
    }

    /**
     * Compacts the map by removing cleared values.
     */
    void compact()
    {
        Reference<? extends V> ref;
        while ( ( ref = queue.poll() ) != null )
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
    private final static class Soft<K, V>
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
    private final static class Weak<K, V>
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
}

/**
 * Thread-safe {@link Map} whose values are kept alive by soft/weak {@link Reference}s.
 */
final class MildConcurrentValues<K, V>
    extends MildValues<K, V>
    implements ConcurrentMap<K, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<K, Reference<V>> concurrentMap;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MildConcurrentValues( final ConcurrentMap<K, Reference<V>> map, final boolean soft )
    {
        super( map, soft );
        this.concurrentMap = map;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public V putIfAbsent( final K key, final V value )
    {
        compact();

        final Reference<V> ref = mildValue( key, value );

        /*
         * We must either add our value to the map, or return a non-null existing value.
         */
        Reference<V> oldRef;
        while ( ( oldRef = concurrentMap.putIfAbsent( key, ref ) ) != null )
        {
            final V oldValue = oldRef.get();
            if ( null != oldValue )
            {
                return oldValue;
            }
            concurrentMap.remove( key, oldRef ); // gone AWOL; remove entry and try again
        }
        return null;
    }

    public V replace( final K key, final V value )
    {
        compact();

        final Reference<V> ref = concurrentMap.replace( key, mildValue( key, value ) );
        return null != ref ? ref.get() : null;
    }

    public boolean replace( final K key, final V oldValue, final V newValue )
    {
        compact();

        return concurrentMap.replace( key, mildValue( null, oldValue ), mildValue( key, newValue ) );
    }

    public boolean remove( final Object key, final Object value )
    {
        compact();

        return concurrentMap.remove( key, tempValue( value ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    void compact()
    {
        Reference<? extends V> ref;
        while ( ( ref = queue.poll() ) != null )
        {
            // only remove this specific key-value mapping; thread-safe
            concurrentMap.remove( ( (InverseMapping) ref ).key(), ref );
        }
    }
}
