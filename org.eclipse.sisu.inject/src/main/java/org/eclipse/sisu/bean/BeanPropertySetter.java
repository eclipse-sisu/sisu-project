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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * {@link BeanProperty} backed by a single-parameter setter {@link Method}.
 */
final class BeanPropertySetter<T> implements BeanProperty<T>, PrivilegedAction<Void> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Method method;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanPropertySetter(final Method method) {
        this.method = method;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        return method.getAnnotation(annotationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TypeLiteral<T> getType() {
        return (TypeLiteral<T>) TypeLiteral.get(method.getGenericParameterTypes()[0]);
    }

    @Override
    public String getName() {
        final String name = method.getName();

        // this is guaranteed OK by the checks made in the BeanProperties code
        return Character.toLowerCase(name.charAt(3)) + name.substring(4);
    }

    @Override
    public <B> void set(final B bean, final T value) {
        if (!method.isAccessible()) {
            // ensure we can update the property
            AccessController.doPrivileged(this); // NOSONAR
        }

        BeanScheduler.detectCycle(value);

        try {
            method.invoke(bean, value);
        } catch (final LinkageError | Exception e) {
            final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
            throw new ProvisionException("Error injecting: " + method, cause);
        }
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public boolean equals(final Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs instanceof BeanPropertySetter<?>) {
            return method.equals(((BeanPropertySetter<?>) rhs).method);
        }
        return false;
    }

    @Override
    public String toString() {
        return method.toString();
    }

    // ----------------------------------------------------------------------
    // PrivilegedAction methods
    // ----------------------------------------------------------------------

    @Override
    public Void run() {
        // enable private injection
        method.setAccessible(true);
        return null;
    }
}
