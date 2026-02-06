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
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * NON-thread-safe {@link Map} whose keys are kept alive by soft/weak {@link Reference}s.
 */
class MildKeys<K, V> implements Map<K, V> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final ReferenceQueue<K> queue = new ReferenceQueue<>();

    final Map<Reference<K>, V> map;

    private final boolean soft;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MildKeys(final Map<Reference<K>, V> map, final boolean soft) {
        this.map = map;
        this.soft = soft;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public final boolean containsKey(final Object key) {
        // skip compact for performance reasons

        return map.containsKey(tempKey(key));
    }

    @Override
    public final boolean containsValue(final Object value) {
        // skip compact for performance reasons

        return map.containsValue(value);
    }

    @Override
    public final V get(final Object key) {
        // skip compact for performance reasons

        return map.get(tempKey(key));
    }

    @Override
    public final V put(final K key, final V value) {
        compact();

        return map.put(mildKey(key), value);
    }

    @Override
    public final void putAll(final Map<? extends K, ? extends V> m) {
        compact();

        for (final Entry<? extends K, ? extends V> e : m.entrySet()) {
            map.put(mildKey(e.getKey()), e.getValue());
        }
    }

    @Override
    public final V remove(final Object key) {
        compact();

        return map.remove(tempKey(key));
    }

    @Override
    public final void clear() {
        map.clear();

        compact();
    }

    @Override
    public final boolean isEmpty() {
        compact();

        return map.isEmpty();
    }

    @Override
    public final int size() {
        compact();

        return map.size();
    }

    @Override
    public final Set<K> keySet() {
        compact();

        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                return new KeyItr();
            }

            @Override
            public int size() {
                return map.size();
            }
        };
    }

    @Override
    public final Collection<V> values() {
        compact();

        return map.values();
    }

    @Override
    public final Set<Entry<K, V>> entrySet() {
        compact();

        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new EntryItr();
            }

            @Override
            public int size() {
                return map.size();
            }
        };
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Soft or weak {@link Reference} for the given key.
     */
    final Reference<K> mildKey(final K key) {
        return soft ? new Soft<>(key, queue) : new Weak<>(key, queue);
    }

    /**
     * @return Temporary {@link Reference} for the given key; used in queries.
     */
    static final <K> Reference<K> tempKey(final K key) {
        return new Weak<>(key, null);
    }

    /**
     * Compacts the map by removing cleared keys.
     */
    final void compact() {
        for (Reference<? extends K> ref; (ref = queue.poll()) != null; ) {
            map.remove(ref);
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Soft key that maintains a constant hash and uses referential equality.
     */
    static class Soft<T> extends SoftReference<T> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final int hash;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Soft(final T o, final ReferenceQueue<T> queue) {
            super(o, queue);
            hash = o.hashCode();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public final int hashCode() {
            return hash;
        }

        @Override
        public final boolean equals(final Object rhs) {
            if (this == rhs) {
                return true; // exact same reference
            }
            final T o = get();
            if (null != o && rhs instanceof Reference<?>) {
                // different reference, but same referent
                return o == ((Reference<?>) rhs).get();
            }
            return false;
        }
    }

    /**
     * Weak key that maintains a constant hash and uses referential equality.
     */
    static class Weak<T> extends WeakReference<T> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final int hash;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Weak(final T o, final ReferenceQueue<T> queue) {
            super(o, queue);
            hash = o.hashCode();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public final int hashCode() {
            return hash;
        }

        @Override
        public final boolean equals(final Object rhs) {
            if (this == rhs) {
                return true; // exact same reference
            }
            final T o = get();
            if (null != o && rhs instanceof Reference<?>) {
                // different reference, but same referent
                return o == ((Reference<?>) rhs).get();
            }
            return false;
        }
    }

    /**
     * {@link Iterator} that iterates over reachable keys in the map.
     */
    final class KeyItr implements Iterator<K> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Reference<K>> itr = map.keySet().iterator();

        private K nextKey;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public boolean hasNext() {
            // find next key that is still reachable
            while (null == nextKey && itr.hasNext()) {
                nextKey = itr.next().get();
            }
            return null != nextKey;
        }

        @Override
        public K next() {
            if (hasNext()) {
                // populated by hasNext()
                final K key = nextKey;
                nextKey = null;
                return key;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            itr.remove();
        }
    }

    /**
     * {@link Iterator} that iterates over reachable entries in the map.
     */
    final class EntryItr implements Iterator<Entry<K, V>> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Entry<Reference<K>, V>> itr = map.entrySet().iterator();

        private Entry<Reference<K>, V> nextEntry;

        private K nextKey;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public boolean hasNext() {
            // find next entry that is still reachable
            while (null == nextKey && itr.hasNext()) {
                nextEntry = itr.next();
                nextKey = nextEntry.getKey().get();
            }
            return null != nextKey;
        }

        @Override
        public Entry<K, V> next() {
            if (hasNext()) {
                // populated by hasNext()
                final Entry<K, V> entry = new StrongEntry(nextEntry, nextKey);
                nextEntry = null;
                nextKey = null;
                return entry;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            itr.remove();
        }
    }

    /**
     * {@link Entry} that delegates to the original entry, but maintains a strong reference to the key.
     */
    final class StrongEntry implements Entry<K, V> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Entry<Reference<K>, V> entry;

        private final K key;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        StrongEntry(final Entry<Reference<K>, V> entry, final K key) {
            this.entry = entry;
            this.key = key;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return entry.getValue();
        }

        @Override
        public V setValue(final V value) {
            return entry.setValue(value);
        }
    }
}
