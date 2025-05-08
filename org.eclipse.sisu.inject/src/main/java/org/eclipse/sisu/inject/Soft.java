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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utility methods for dealing with {@link SoftReference} collections.
 */
@SuppressWarnings( { "rawtypes", "unchecked" } )
public final class Soft
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Soft()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * @return {@link Collection} whose elements are kept alive with {@link SoftReference}s
     */
    public static <T> Collection<T> elements()
    {
        return elements( 10 );
    }

    /**
     * @param capacity The initial capacity
     * @return {@link Collection} whose elements are kept alive with {@link SoftReference}s
     */
    public static <T> Collection<T> elements( final int capacity )
    {
        return new MildElements( new ArrayList( capacity ), true );
    }

    /**
     * @return {@link Map} whose keys are kept alive with {@link SoftReference}s
     */
    public static <K, V> Map<K, V> keys()
    {
        return keys( 16 );
    }

    /**
     * @param capacity The initial capacity
     * @return {@link Map} whose keys are kept alive with {@link SoftReference}s
     */
    public static <K, V> Map<K, V> keys( final int capacity )
    {
        return new MildKeys( new HashMap( capacity ), true );
    }

    /**
     * @return {@link ConcurrentMap} whose keys are kept alive with {@link SoftReference}s
     */
    public static <K, V> ConcurrentMap<K, V> concurrentKeys()
    {
        return concurrentKeys( 16, 1 );
    }

    /**
     * @param capacity The initial capacity
     * @param concurrency The concurrency level
     * @return {@link ConcurrentMap} whose keys are kept alive with {@link SoftReference}s
     */
    public static <K, V> ConcurrentMap<K, V> concurrentKeys( final int capacity, final int concurrency )
    {
        return new MildConcurrentKeys( new ConcurrentHashMap( capacity, 0.75f, concurrency ), true );
    }

    /**
     * @return {@link Map} whose values are kept alive with {@link SoftReference}s
     */
    public static <K, V> Map<K, V> values()
    {
        return values( 16 );
    }

    /**
     * @param capacity The initial capacity
     * @return {@link Map} whose values are kept alive with {@link SoftReference}s
     */
    public static <K, V> Map<K, V> values( final int capacity )
    {
        return new MildValues( new HashMap( capacity ), true );
    }

    /**
     * @return {@link ConcurrentMap} whose values are kept alive with {@link SoftReference}s
     */
    public static <K, V> ConcurrentMap<K, V> concurrentValues()
    {
        return concurrentValues( 16, 1 );
    }

    /**
     * @param capacity The initial capacity
     * @param concurrency The concurrency level
     * @return {@link ConcurrentMap} whose values are kept alive with {@link SoftReference}s
     */
    public static <K, V> ConcurrentMap<K, V> concurrentValues( final int capacity, final int concurrency )
    {
        return new MildConcurrentValues( new ConcurrentHashMap( capacity, 0.75f, concurrency ), true );
    }
}
