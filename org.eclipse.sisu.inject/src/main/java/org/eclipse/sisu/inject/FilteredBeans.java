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

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.eclipse.sisu.BeanEntry;

/**
 * Sequence of {@link BeanEntry}s filtered according to supplied {@link java.util.function.Predicate}.
 * This class is public, as it is reused in Plexus Shim for realm filtering.
 */
public final class FilteredBeans<Q extends Annotation, T> implements Iterable<BeanEntry<Q, T>> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Supplier<Predicate<BeanEntry<Q, T>>> predicateSupplier;

    final Iterable<BeanEntry<Q, T>> beans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public FilteredBeans(
            final Supplier<Predicate<BeanEntry<Q, T>>> predicateSupplier, final Iterable<BeanEntry<Q, T>> beans) {
        this.predicateSupplier = predicateSupplier;
        this.beans = beans;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Iterator<BeanEntry<Q, T>> iterator() {
        final Predicate<BeanEntry<Q, T>> predicate = predicateSupplier != null ? predicateSupplier.get() : null;
        if (null != predicate) {
            return new FilteredItr(predicate);
        }
        return beans.iterator();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link BeanEntry} iterator that only returns entries allowed by predicate.
     */
    final class FilteredItr implements Iterator<BeanEntry<Q, T>> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<BeanEntry<Q, T>> itr = beans.iterator();

        private final Predicate<BeanEntry<Q, T>> predicate;

        private BeanEntry<Q, T> nextBean;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        public FilteredItr(final Predicate<BeanEntry<Q, T>> predicate) {
            this.predicate = predicate;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public boolean hasNext() {
            if (null != nextBean) {
                return true;
            }
            while (itr.hasNext()) {
                nextBean = itr.next();
                if (predicate.test(nextBean)) {
                    return true;
                }
            }
            nextBean = null;
            return false;
        }

        @Override
        public BeanEntry<Q, T> next() {
            if (hasNext()) {
                // populated by hasNext()
                final BeanEntry<Q, T> bean = nextBean;
                nextBean = null;
                return bean;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
