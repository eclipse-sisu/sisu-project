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
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvidedBy;
import com.google.inject.TypeLiteral;
import org.eclipse.sisu.Hidden;

/**
 * Source of "implicit" bindings; includes @{@link ImplementedBy}, @{@link ProvidedBy}, and concrete types.
 */
final class ImplicitBindings {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<BindingPublisher> publishers;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ImplicitBindings(final Iterable<BindingPublisher> publishers) {
        this.publishers = publishers;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Binding<T> get(final TypeLiteral<T> type) {
        // first round: check for any re-written implicit bindings

        final Key implicitKey = TypeArguments.implicitKey(type.getRawType());
        for (final BindingPublisher p : publishers) {
            final Injector injector = p.adapt(Injector.class);
            if (null != injector) {
                final Binding binding = injector.getBindings().get(implicitKey);
                if (null != binding) {
                    Logs.trace("Using implicit binding: {} from: <>", binding, injector);
                    return binding;
                }
            }
        }

        // second round: fall back to just-in-time binding lookup

        final Key justInTimeKey = Key.get(type);
        for (final BindingPublisher p : publishers) {
            final Injector injector = p.adapt(Injector.class);
            if (null != injector) {
                try {
                    final Binding binding = injector.getBinding(justInTimeKey);
                    if (null == Sources.getAnnotation(binding, Hidden.class)) {
                        Logs.trace("Using just-in-time binding: {} from: <>", binding, injector);
                        return binding;
                    }
                } catch (final LinkageError | RuntimeException e) {
                    Logs.debug("Problem with just-in-time binding: {}", justInTimeKey, e);
                }
            }
        }
        return null;
    }
}
