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
package org.eclipse.sisu.bean;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * {@link BeanProperty} backed by a {@link Field}.
 */
final class BeanPropertyField<T> implements BeanProperty<T>, PrivilegedAction<Void> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Field field;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanPropertyField(final Field field) {
        this.field = field;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        return field.getAnnotation(annotationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TypeLiteral<T> getType() {
        return (TypeLiteral<T>) TypeLiteral.get(field.getGenericType());
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public <B> void set(final B bean, final T value) {
        if (!field.isAccessible()) {
            // make sure we can apply the binding
            AccessController.doPrivileged(this); // NOSONAR
        }

        BeanScheduler.detectCycle(value);

        try {
            field.set(bean, value);
        } catch (final LinkageError | Exception e) {
            throw new ProvisionException("Error injecting: " + field, e);
        }
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public boolean equals(final Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs instanceof BeanPropertyField<?>) {
            return field.equals(((BeanPropertyField<?>) rhs).field);
        }
        return false;
    }

    @Override
    public String toString() {
        return field.toString();
    }

    // ----------------------------------------------------------------------
    // PrivilegedAction methods
    // ----------------------------------------------------------------------

    @Override
    public Void run() {
        // enable private injection
        field.setAccessible(true);
        return null;
    }
}
