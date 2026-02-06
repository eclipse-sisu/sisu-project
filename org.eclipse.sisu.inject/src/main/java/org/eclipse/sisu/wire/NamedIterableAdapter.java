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

import com.google.inject.name.Named;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * String mapping {@link Iterable} backed by a {@link Named} mapping {@link Iterable}.
 */
final class NamedIterableAdapter<V> implements Iterable<Entry<String, V>> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Entry<Named, V>> delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    NamedIterableAdapter(final Iterable<Entry<Named, V>> delegate) {
        this.delegate = delegate;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Iterator<Entry<String, V>> iterator() {
        return new NamedIterator<>(delegate);
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * String mapping {@link Iterator} backed by a {@link Named} mapping {@link Iterator}.
     */
    private static final class NamedIterator<V> implements Iterator<Entry<String, V>> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Entry<Named, V>> iterator;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        NamedIterator(final Iterable<Entry<Named, V>> iterable) {
            iterator = iterable.iterator();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<String, V> next() {
            return new NamedEntry<>(iterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * String mapping {@link Entry} backed by a {@link Named} mapping {@link Entry}.
     */
    private static final class NamedEntry<V> implements Entry<String, V> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Entry<Named, V> entry;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        NamedEntry(final Entry<Named, V> entry) {
            this.entry = entry;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public String getKey() {
            return entry.getKey().value();
        }

        @Override
        public V getValue() {
            return entry.getValue();
        }

        @Override
        public V setValue(final V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }
}
