/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.inject;

import java.lang.ref.Reference;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe {@link Map} whose keys are kept alive by soft/weak {@link Reference}s.
 */
final class MildConcurrentKeys<K, V>
    extends MildKeys<K, V>
    implements ConcurrentMap<K, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<Reference<K>, V> concurrentMap;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MildConcurrentKeys( final ConcurrentMap<Reference<K>, V> map, final boolean soft )
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

        return concurrentMap.putIfAbsent( mildKey( key ), value );
    }

    public V replace( final K key, final V value )
    {
        compact();

        return concurrentMap.replace( mildKey( key ), value );
    }

    public boolean replace( final K key, final V oldValue, final V newValue )
    {
        compact();

        return concurrentMap.replace( mildKey( key ), oldValue, newValue );
    }

    public boolean remove( final Object key, final Object value )
    {
        compact();

        return concurrentMap.remove( tempKey( key ), value );
    }
}
