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
package org.eclipse.sisu.space;

import org.eclipse.sisu.inject.DeferredClass;

/**
 * {@link DeferredClass} representing a named class from a {@link ClassSpace}.
 */
final class NamedClass<T> extends AbstractDeferredClass<T> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final String name;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    NamedClass(final ClassSpace space, final String name) {
        this.space = space;
        this.name = name;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> load() {
        return (Class<T>) space.loadClass(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return (17 * 31 + name.hashCode()) * 31 + space.hashCode();
    }

    @Override
    public boolean equals(final Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs instanceof NamedClass<?>) {
            final NamedClass<?> clazz = (NamedClass<?>) rhs;
            return name.equals(clazz.name) && space.equals(clazz.space);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Deferred " + name + " from " + space;
    }
}
