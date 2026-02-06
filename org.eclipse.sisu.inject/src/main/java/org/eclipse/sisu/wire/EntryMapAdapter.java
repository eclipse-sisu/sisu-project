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
package org.eclipse.sisu.wire;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * {@link Map} backed by an {@link Iterable} sequence of map entries.
 */
public final class EntryMapAdapter<K, V> extends AbstractMap<K, V> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Entry<K, V>> entrySet;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public EntryMapAdapter(final Iterable<? extends Entry<K, V>> iterable) {
        entrySet = new EntrySet<>(iterable);
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet;
    }

    @Override
    public boolean isEmpty() {
        return entrySet.isEmpty();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Entry {@link Set} backed by an {@link Iterable} sequence of map entries.
     */
    private static final class EntrySet<K, V> extends AbstractSet<Entry<K, V>> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterable<Entry<K, V>> iterable;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        @SuppressWarnings("unchecked")
        EntrySet(final Iterable<? extends Entry<K, V>> iterable) {
            this.iterable = (Iterable<Entry<K, V>>) iterable;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return iterable.iterator();
        }

        @Override
        public boolean isEmpty() {
            return false == iterator().hasNext();
        }

        @Override
        public int size() {
            int size = 0;
            for (final Iterator<?> i = iterable.iterator(); i.hasNext(); i.next()) {
                size++;
            }
            return size;
        }
    }
}
