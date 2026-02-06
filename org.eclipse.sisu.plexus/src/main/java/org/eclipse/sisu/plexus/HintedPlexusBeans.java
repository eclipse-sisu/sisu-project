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

import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.sisu.BeanEntry;

/**
 * Hint-driven sequence of {@link PlexusBean}s that uses {@link MissingPlexusBean}s to indicate missing hints.
 */
final class HintedPlexusBeans<T> implements Iterable<PlexusBean<T>> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<BeanEntry<Named, T>> beans;

    private final List<PlexusBean<T>> missingPlexusBeans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    HintedPlexusBeans(final Iterable<BeanEntry<Named, T>> beans, final TypeLiteral<T> role, final String[] hints) {
        this.beans = beans;

        missingPlexusBeans = new ArrayList<>(hints.length);
        for (final String h : hints) {
            missingPlexusBeans.add(new MissingPlexusBean<>(role, h));
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Iterator<PlexusBean<T>> iterator() {
        // assume all hints are missing to begin with
        final List<PlexusBean<T>> plexusBeans = new ArrayList<>(missingPlexusBeans);

        // scan available beans and populate list according to hint
        final int size = plexusBeans.size();
        final Iterator<BeanEntry<Named, T>> itr = beans.iterator();
        for (int numFound = 0; numFound < size && itr.hasNext(); ) {
            final BeanEntry<Named, T> candidate = itr.next();
            final String hint = candidate.getKey().value();
            for (int i = 0; i < size; i++) {
                final PlexusBean<T> element = plexusBeans.get(i);
                if (element instanceof MissingPlexusBean<?> && hint.equals(element.getKey())) {
                    plexusBeans.set(i, new LazyPlexusBean<>(candidate));
                    numFound++;
                }
            }
        }

        return plexusBeans.iterator();
    }
}
