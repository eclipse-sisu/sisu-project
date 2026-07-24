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
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.sisu.BeanEntry;

/**
 * Sequence of {@link BeanEntry}s filtered according to whether they are visible from the current {@link ClassRealm}.
 */
final class RealmFilteredBeans<T> implements Iterable<BeanEntry<Named, T>> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final RealmManager realmManager;

    final Iterable<BeanEntry<Named, T>> beans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RealmFilteredBeans(final RealmManager realmManager, final Iterable<BeanEntry<Named, T>> beans) {
        this.realmManager = realmManager;
        this.beans = beans;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Iterator<BeanEntry<Named, T>> iterator() {
        Predicate<BeanEntry<Named, T>> predicate = null != realmManager ? realmManager.visibilityPredicate() : null;
        if (null != predicate) {
            return new FilteredItr(predicate);
        }
        return beans.iterator();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link BeanEntry} iterator that only returns entries visible from the given set of named realms.
     */
    final class FilteredItr implements Iterator<BeanEntry<Named, T>> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<BeanEntry<Named, T>> itr = beans.iterator();

        private final Predicate<BeanEntry<Named, T>> predicate;

        private BeanEntry<Named, T> nextBean;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        public FilteredItr(final Predicate<BeanEntry<Named, T>> predicate) {
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
        public BeanEntry<Named, T> next() {
            if (hasNext()) {
                // populated by hasNext()
                final BeanEntry<Named, T> bean = nextBean;
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
