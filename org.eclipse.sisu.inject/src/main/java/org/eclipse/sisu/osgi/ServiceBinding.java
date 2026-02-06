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
package org.eclipse.sisu.osgi;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ElementVisitor;
import org.eclipse.sisu.inject.BindingSubscriber;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * Service {@link Binding} backed by an OSGi {@link ServiceReference}.
 */
final class ServiceBinding<T> implements Binding<T>, Provider<T> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Key<T> key;

    private final T instance;

    private final int rank;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ServiceBinding(
            final BundleContext context, final String clazzName, final int maxRank, final ServiceReference<T> reference)
            throws ClassNotFoundException {
        @SuppressWarnings("unchecked")
        final Class<T> clazz = (Class<T>) reference.getBundle().loadClass(clazzName);
        final Object name = reference.getProperty("name");
        if (name instanceof String && ((String) name).length() > 0) {
            key = Key.get(clazz, Names.named((String) name));
        } else {
            key = Key.get(clazz);
        }

        instance = context.getService(reference);

        if (maxRank > Integer.MIN_VALUE) {
            final int serviceRanking = getServiceRanking(reference);
            rank = serviceRanking < maxRank ? serviceRanking : maxRank;
        } else {
            rank = Integer.MIN_VALUE;
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Key<T> getKey() {
        return key;
    }

    @Override
    public Provider<T> getProvider() {
        return this;
    }

    @Override
    public T get() {
        return instance;
    }

    @Override
    public Object getSource() {
        return "OSGi service registry";
    }

    @Override
    public void applyTo(final Binder binder) {
        // no-op
    }

    @Override
    public <V> V acceptVisitor(final ElementVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V> V acceptTargetVisitor(final BindingTargetVisitor<? super T, V> visitor) {
        return null;
    }

    @Override
    public <V> V acceptScopingVisitor(final BindingScopingVisitor<V> visitor) {
        return visitor.visitScopeAnnotation(Singleton.class);
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    boolean isCompatibleWith(final BindingSubscriber<T> subscriber) {
        return key.getTypeLiteral().getRawType().equals(subscriber.type().getRawType());
    }

    int rank() {
        return rank;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static int getServiceRanking(final ServiceReference<?> reference) {
        final Object ranking = reference.getProperty(Constants.SERVICE_RANKING);
        return ranking instanceof Integer ? ((Integer) ranking).intValue() : 0;
    }
}
