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
import java.lang.annotation.Annotation;
import org.eclipse.sisu.Description;
import org.eclipse.sisu.Hidden;
import org.eclipse.sisu.Priority;

/**
 * Utility methods for dealing with annotated sources.
 */
public final class Sources {
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Sources() {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Hides a new binding source from the bean locator.
     *
     * @return Hidden source
     */
    public static Hidden hide() {
        return hide(null);
    }

    /**
     * Hides the given binding source from the bean locator.
     *
     * @param source The source
     * @return Hidden source
     */
    public static Hidden hide(final Object source) {
        return new HiddenSource(source);
    }

    /**
     * Describes a new binding source with the given description.
     *
     * @param value The description
     * @return Described source
     */
    public static Description describe(final String value) {
        return describe(null, value);
    }

    /**
     * Describes the given binding source with the given description.
     *
     * @param source The source
     * @param value The description
     * @return Described source
     */
    public static Description describe(final Object source, final String value) {
        return new DescriptionSource(source, value);
    }

    /**
     * Prioritizes a new binding source with the given priority.
     *
     * @param value The priority
     * @return Prioritized source
     */
    public static Priority prioritize(final int value) {
        return prioritize(null, value);
    }

    /**
     * Prioritizes the given binding source with the given priority.
     *
     * @param source The source
     * @param value The priority
     * @return Prioritized source
     */
    public static Priority prioritize(final Object source, final int value) {
        return new PrioritySource(source, value);
    }

    /**
     * Searches the binding's source and implementation for an annotation of the given type.
     *
     * @param binding The binding
     * @param annotationType The annotation type
     * @return Annotation instance; {@code null} if it doesn't exist
     */
    public static <T extends Annotation> T getAnnotation(final Binding<?> binding, final Class<T> annotationType) {
        T annotation = null;
        final Object source = Guice4.getDeclaringSource(binding);
        if (source instanceof AnnotatedSource) {
            annotation = ((AnnotatedSource) source).getAnnotation(binding, annotationType);
        }
        if (null == annotation) {
            annotation = Implementations.getAnnotation(binding, annotationType);
        }
        return annotation;
    }
}
