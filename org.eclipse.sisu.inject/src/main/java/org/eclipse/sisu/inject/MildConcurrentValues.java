/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
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
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe {@link Map} whose values are kept alive by soft/weak {@link Reference}s.
 */
final class MildConcurrentValues<K, V> extends MildValues<K, V> implements ConcurrentMap<K, V> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<K, Reference<V>> concurrentMap;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MildConcurrentValues(final ConcurrentMap<K, Reference<V>> map, final boolean soft) {
        super(map, soft);
        this.concurrentMap = map;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public V putIfAbsent(final K key, final V value) {
        compact();

        final Reference<V> ref = mildValue(key, value);

        /*
         * We must either add our value to the map, or return a non-null existing value.
         */
        for (Reference<V> oldRef; (oldRef = concurrentMap.putIfAbsent(key, ref)) != null; ) {
            final V oldValue = oldRef.get();
            if (null != oldValue) {
                return oldValue;
            }
            concurrentMap.remove(key, oldRef); // gone AWOL; remove entry and try again
        }
        return null;
    }

    @Override
    public V replace(final K key, final V value) {
        compact();

        final Reference<V> ref = concurrentMap.replace(key, mildValue(key, value));
        return null != ref ? ref.get() : null;
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        compact();

        return concurrentMap.replace(key, tempValue(oldValue), mildValue(key, newValue));
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        compact(); // NOSONAR ignore nullable false-positive

        return concurrentMap.remove(key, tempValue(value));
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    void compact() {
        for (Reference<? extends V> ref; (ref = queue.poll()) != null; ) {
            // only remove this specific key-value mapping; thread-safe
            concurrentMap.remove(((InverseMapping) ref).key(), ref);
        }
    }
}
