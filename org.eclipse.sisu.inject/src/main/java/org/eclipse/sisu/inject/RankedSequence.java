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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Ordered {@link List} that arranges elements by descending rank; supports concurrent iteration and modification.
 */
final class RankedSequence<T> implements Iterable<T> {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<RankedSequence, Content> CONTENT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(RankedSequence.class, Content.class, "content");

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    volatile Content content; // NOSONAR

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RankedSequence() {
        // nothing to do
    }

    RankedSequence(final RankedSequence<T> sequence) {
        if (null != sequence) {
            content = sequence.content;
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Inserts the given element into the ordered list, using the assigned rank as a guide.
     * <p>
     * The rank can be any value from {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}.
     *
     * @param element The element to insert
     * @param rank The assigned rank
     */
    public void insert(final T element, final int rank) {
        Content o, n;
        do {
            n = null != (o = content) ? o.insert(element, rank) : new Content(element, rank);
        } while (!CONTENT_UPDATER.compareAndSet(this, o, n));
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        final Content snapshot = content;
        return null != snapshot ? (T) snapshot.objs[0] : null;
    }

    public boolean contains(final Object element) {
        final Content snapshot = content;
        return null != snapshot && snapshot.indexOf(element) >= 0;
    }

    public boolean containsThis(final Object element) {
        final Content snapshot = content;
        return null != snapshot && snapshot.indexOfThis(element) >= 0;
    }

    @SuppressWarnings("unchecked")
    public T remove(final Object element) {
        Content o, n;
        int index;
        do {
            if (null == (o = content) || (index = o.indexOf(element)) < 0) {
                return null;
            }
            n = o.remove(index);
        } while (!CONTENT_UPDATER.compareAndSet(this, o, n));

        return (T) o.objs[index];
    }

    public boolean removeThis(final T element) {
        Content o, n;
        do {
            final int index;
            if (null == (o = content) || (index = o.indexOfThis(element)) < 0) {
                return false;
            }
            n = o.remove(index);
        } while (!CONTENT_UPDATER.compareAndSet(this, o, n));

        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Iterable<T> snapshot() {
        final Content snapshot = content;
        return null != snapshot ? (List) Arrays.asList(snapshot.objs) : Collections.EMPTY_SET;
    }

    public void clear() {
        content = null;
    }

    public boolean isEmpty() {
        return null == content;
    }

    public int size() {
        final Content snapshot = content;
        return null != snapshot ? snapshot.objs.length : 0;
    }

    @Override
    public Itr iterator() {
        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Turns the given (potentially non-unique) rank into a unique id by appending a counter.
     *
     * @param rank The assigned rank
     * @param uniq The unique counter
     * @return The unique id
     */
    static long rank2uid(final int rank, final int uniq) {
        return (long) ~rank << 32 | 0x00000000FFFFFFFFL & uniq;
    }

    /**
     * Extracts the original (potentially non-unique) assigned rank from the given unique id.
     *
     * @param uid The unique id
     * @return Assigned rank
     */
    static int uid2rank(final long uid) {
        return (int) (~uid >>> 32);
    }

    /**
     * Finds the insertion point with the nearest UID, regardless of whether the UID is in the list or not.
     * <p>
     * Unlike {@link Arrays#binarySearch} this will always return a number from zero to {@link #size()} inclusive.
     *
     * @param uids The UIDs array
     * @param uid The UID to find
     * @return Index with nearest UID
     */
    static int safeBinarySearch(final long[] uids, final long uid) {
        if (uid < uids[0]) {
            return 0;
        }
        int min = 0;
        int max = uids.length - 1;
        while (min < max) {
            final int m = min + max >>> 1;
            if (uid <= uids[m]) {
                max = m;
            } else {
                min = m + 1;
            }
        }
        if (min == uids.length - 1 && uids[min] < uid) {
            min++; // append
        }
        return min;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Represents an immutable snapshot of ranked elements.
     */
    static final class Content {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        final Object[] objs;

        final long[] uids;

        final int uniq;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Content(final Object element, final int rank) {
            objs = new Object[] {element};
            uids = new long[] {rank2uid(rank, 0)};
            uniq = 1;
        }

        Content(final Object[] objs, final long[] uids, final int uniq) // NOSONAR
                {
            this.objs = objs;
            this.uids = uids;
            this.uniq = uniq;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public int indexOf(final Object element) {
            if (null == element) {
                return indexOfThis(null);
            }
            for (int i = 0; i < objs.length; i++) {
                if (element.equals(objs[i])) {
                    return i;
                }
            }
            return -1;
        }

        public int indexOfThis(final Object element) {
            for (int i = 0; i < objs.length; i++) {
                if (element == objs[i]) {
                    return i;
                }
            }
            return -1;
        }

        public Content insert(final Object element, final int rank) {
            final int size = objs.length + 1;

            final Object[] newObjs = new Object[size];
            final long[] newUIDs = new long[size];

            final long uid = rank2uid(rank, uniq);
            final int index = safeBinarySearch(uids, uid);
            if (index > 0) {
                System.arraycopy(objs, 0, newObjs, 0, index);
                System.arraycopy(uids, 0, newUIDs, 0, index);
            }

            newObjs[index] = element;
            newUIDs[index] = uid;

            final int destPos = index + 1, len = size - destPos;
            if (len > 0) {
                System.arraycopy(objs, index, newObjs, destPos, len);
                System.arraycopy(uids, index, newUIDs, destPos, len);
            }

            return new Content(newObjs, newUIDs, uniq + 1);
        }

        public Content remove(final int index) {
            if (objs.length == 1) {
                return null;
            }

            final int size = objs.length - 1;

            final Object[] newObjs = new Object[size];
            final long[] newUIDs = new long[size];

            if (index > 0) {
                System.arraycopy(objs, 0, newObjs, 0, index);
                System.arraycopy(uids, 0, newUIDs, 0, index);
            }
            final int srcPos = index + 1, len = size - index;
            if (len > 0) {
                System.arraycopy(objs, srcPos, newObjs, index, len);
                System.arraycopy(uids, srcPos, newUIDs, index, len);
            }

            return new Content(newObjs, newUIDs, uniq);
        }
    }

    /**
     * Custom {@link Iterator} that copes with modification by repositioning itself in the updated list.
     */
    final class Itr implements Iterator<T> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private Content snapshot;

        private T nextObj;

        private long nextUID = Long.MIN_VALUE;

        private int index = -1;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        @SuppressWarnings("unchecked")
        public boolean hasNext() {
            if (null != nextObj) {
                return true;
            }
            final Content newSnapshot = content;
            if (snapshot != newSnapshot) {
                index = null != newSnapshot ? safeBinarySearch(newSnapshot.uids, nextUID) : -1;
                snapshot = newSnapshot;
            }
            if (index >= 0 && index < snapshot.objs.length) // NOSONAR
            {
                nextObj = (T) snapshot.objs[index];
                nextUID = snapshot.uids[index];
                return true;
            }
            return false;
        }

        /**
         * @return {@code true} if the next element is ranked at or above the given rank; otherwise {@code false}
         */
        public boolean hasNext(final int rank) {
            if (null != nextObj) {
                return uid2rank(nextUID) >= rank;
            }
            final Content newSnapshot = content;
            if (snapshot != newSnapshot) {
                index = null != newSnapshot ? safeBinarySearch(newSnapshot.uids, nextUID) : -1;
                snapshot = newSnapshot;
            }
            if (index >= 0 && index < snapshot.uids.length) // NOSONAR
            {
                return uid2rank(snapshot.uids[index]) >= rank;
            }
            return false;
        }

        @Override
        public T next() {
            if (hasNext()) {
                nextUID++; // guarantees progress when re-positioning
                index++;

                // populated by hasNext()
                final T element = nextObj;
                nextObj = null;
                return element;
            }
            throw new NoSuchElementException();
        }

        public int rank() {
            return uid2rank(nextUID);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
