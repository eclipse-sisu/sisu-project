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

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;

/**
 * Provides dynamic {@link BeanEntry} notifications by tracking qualified {@link Binding}s.
 *
 * @see BeanLocator#watch(Key, Mediator, Object)
 */
final class WatchedBeans<Q extends Annotation, T, W> implements BindingSubscriber<T> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanCache<Q, T> beans = new BeanCache<>();

    private final Key<T> key;

    private final Mediator<Q, T, W> mediator;

    private final QualifyingStrategy strategy;

    private final Reference<W> watcherRef;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    WatchedBeans(final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher) {
        this.key = key;
        this.mediator = mediator;

        strategy = QualifyingStrategy.selectFor(key);
        watcherRef = new WeakReference<>(watcher);
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public TypeLiteral<T> type() {
        return key.getTypeLiteral();
    }

    @Override
    public void add(final Binding<T> binding, final int rank) {
        @SuppressWarnings("unchecked")
        final Q qualifier = (Q) strategy.qualifies(key, binding);
        if (null != qualifier) {
            final W watcher = watcherRef.get();
            if (null != watcher) {
                final BeanEntry<Q, T> bean = beans.create(qualifier, binding, rank);
                try {
                    mediator.add(bean, watcher);
                } catch (final Throwable e) // NOSONAR see Logs.catchThrowable
                {
                    Logs.catchThrowable(e);
                    Logs.warn("Problem adding: <> to: " + detail(watcher), bean, e);
                }
            }
        }
    }

    @Override
    public void remove(final Binding<T> binding) {
        final BeanEntry<Q, T> bean = beans.remove(binding);
        if (null != bean) {
            final W watcher = watcherRef.get();
            if (null != watcher) {
                try {
                    mediator.remove(bean, watcher);
                } catch (final Throwable e) // NOSONAR see Logs.catchThrowable
                {
                    Logs.catchThrowable(e);
                    Logs.warn("Problem removing: <> from: " + detail(watcher), bean, e);
                }
            }
        }
    }

    @Override
    public Iterable<Binding<T>> bindings() {
        return beans.bindings();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private String detail(final Object watcher) {
        return Logs.identityToString(watcher) + " via: " + Logs.identityToString(mediator);
    }
}
