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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utility methods for dealing with {@link WeakReference} collections.
 */
@SuppressWarnings( { "rawtypes", "unchecked" } )
public final class Weak
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Weak()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * @return {@link Collection} whose elements are kept alive with {@link WeakReference}s
     */
    public static <T> Collection<T> elements()
    {
        return elements( 10 );
    }

    /**
     * @param capacity The initial capacity
     * @return {@link Collection} whose elements are kept alive with {@link WeakReference}s
     */
    public static <T> Collection<T> elements( final int capacity )
    {
        return new MildElements( new ArrayList( capacity ), false );
    }

    /**
     * @return {@link Map} whose keys are kept alive with {@link WeakReference}s
     */
    public static <K, V> Map<K, V> keys()
    {
        return keys( 16 );
    }

    /**
     * @param capacity The initial capacity
     * @return {@link Map} whose keys are kept alive with {@link WeakReference}s
     */
    public static <K, V> Map<K, V> keys( final int capacity )
    {
        return new MildKeys( new HashMap( capacity ), false );
    }

    /**
     * @return {@link ConcurrentMap} whose keys are kept alive with {@link WeakReference}s
     */
    public static <K, V> ConcurrentMap<K, V> concurrentKeys()
    {
        return concurrentKeys( 16, 4 );
    }

    /**
     * @param capacity The initial capacity
     * @param concurrency The concurrency level
     * @return {@link ConcurrentMap} whose keys are kept alive with {@link WeakReference}s
     */
    public static <K, V> ConcurrentMap<K, V> concurrentKeys( final int capacity, final int concurrency )
    {
        return new MildConcurrentKeys( new ConcurrentHashMap( capacity, 0.75f, concurrency ), false );
    }

    /**
     * @return {@link Map} whose values are kept alive with {@link WeakReference}s
     */
    public static <K, V> Map<K, V> values()
    {
        return values( 16 );
    }

    /**
     * @param capacity The initial capacity
     * @return {@link Map} whose values are kept alive with {@link WeakReference}s
     */
    public static <K, V> Map<K, V> values( final int capacity )
    {
        return new MildValues( new HashMap( capacity ), false );
    }

    /**
     * @return {@link ConcurrentMap} whose values are kept alive with {@link WeakReference}s
     */
    public static <K, V> ConcurrentMap<K, V> concurrentValues()
    {
        return concurrentValues( 16, 4 );
    }

    /**
     * @param capacity The initial capacity
     * @param concurrency The concurrency level
     * @return {@link ConcurrentMap} whose values are kept alive with {@link WeakReference}s
     */
    public static <K, V> ConcurrentMap<K, V> concurrentValues( final int capacity, final int concurrency )
    {
        return new MildConcurrentValues( new ConcurrentHashMap( capacity, 0.75f, concurrency ), false );
    }
}
