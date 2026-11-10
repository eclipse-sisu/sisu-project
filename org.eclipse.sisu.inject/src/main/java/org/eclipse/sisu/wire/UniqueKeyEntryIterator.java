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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import org.eclipse.sisu.inject.Logs;

/**
 * {@link Iterator} of map entries that filters out entries with duplicate keys, keeping only the first entry for each key.
 * It emits a warning for each ignored entry, including the classloader of the entry's value to help identify the source of the duplicate key.
 */
public class UniqueKeyEntryIterator<K, V> implements Iterator<Entry<K, V>> {

    private final Iterator<Entry<K, V>> delegate;

    private final Set<K> seenKeys = new HashSet<>();

    private Entry<K, V> nextEntry;

    private boolean fetched = false;

    public UniqueKeyEntryIterator(final Iterator<Entry<K, V>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        prefetch();
        return nextEntry != null;
    }

    @Override
    public Entry<K, V> next() {
        prefetch();
        if (nextEntry == null) {
            throw new NoSuchElementException();
        }
        final Entry<K, V> result = nextEntry;
        nextEntry = null;
        fetched = false;
        return result;
    }

    private void prefetch() {
        if (fetched) {
            return;
        }
        fetched = true;
        nextEntry = null;
        while (delegate.hasNext()) {
            final Entry<K, V> candidate = delegate.next();
            if (seenKeys.add(candidate.getKey())) {
                nextEntry = candidate;
                break;
            } else {
                Logs.warn(
                        "Ignoring entry with duplicate key {}: {}",
                        candidate.getKey(),
                        candidate.getValue() + " (loaded via classloader "
                                + candidate.getValue().getClass().getClassLoader() + ") ");
            }
        }
    }
}
