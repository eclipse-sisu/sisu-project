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

/**
 * Something that can visit annotation declarations.
 */
public interface AnnotationVisitor {
    /**
     * Enters the annotation declaration.
     */
    void enterAnnotation();

    /**
     * Visits an element of the declared annotation.
     *
     * <p>
     * The value is delivered in a normalized form, independent of the class
     * file implementation that performs the scanning:
     * <ul>
     * <li>primitive elements ({@code byte}, {@code char}, {@code short},
     *     {@code int}, {@code long}, {@code float}, {@code double},
     *     {@code boolean}) are delivered as their boxed forms</li>
     * <li>{@code String} elements are delivered as {@link String}s</li>
     * <li>enum constant elements are delivered as the {@link String} binary
     *     name of the enum class (the constant name is not delivered)</li>
     * <li>class literal elements are delivered as the {@link String} binary
     *     name of the referenced class</li>
     * <li>arrays of the above are delivered as {@code Object[]} with
     *     element-wise normalized values</li>
     * <li>nested annotation elements are not delivered</li>
     * </ul>
     *
     * @param name The element name
     * @param value The normalized element value
     */
    void visitElement(String name, Object value);

    /**
     * Leaves the annotation declaration.
     */
    void leaveAnnotation();
}
