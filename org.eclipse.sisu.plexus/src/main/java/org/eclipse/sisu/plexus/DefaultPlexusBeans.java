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
package org.eclipse.sisu.plexus;

import com.google.inject.name.Named;
import java.util.Iterator;
import org.eclipse.sisu.BeanEntry;

/**
 * Sequence of {@link PlexusBean}s backed by {@link BeanEntry}s.
 */
final class DefaultPlexusBeans<T> implements Iterable<PlexusBean<T>> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    Iterable<BeanEntry<Named, T>> beans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DefaultPlexusBeans(final Iterable<BeanEntry<Named, T>> beans) {
        this.beans = beans;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Iterator<PlexusBean<T>> iterator() {
        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link PlexusBean} iterator backed by {@link BeanEntry}s.
     */
    final class Itr implements Iterator<PlexusBean<T>> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<BeanEntry<Named, T>> itr = beans.iterator();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public PlexusBean<T> next() {
            return new LazyPlexusBean<>(itr.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
