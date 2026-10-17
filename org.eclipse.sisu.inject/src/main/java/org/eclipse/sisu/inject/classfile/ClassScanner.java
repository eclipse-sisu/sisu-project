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
package org.eclipse.sisu.inject.classfile;

import org.eclipse.sisu.space.ClassVisitor;

/**
 * Scans a single class file and dispatches its contents to a {@link ClassVisitor}.
 *
 * <p>
 * This is an internal Sisu SPI, not a public API: implementations are selected
 * at runtime by {@link ClassFileSupport}.
 *
 * <p>
 * Implementations must dispatch the class annotations in the order they appear in
 * the class file, and must deliver normalized values to
 * {@link org.eclipse.sisu.space.AnnotationVisitor#visitElement(String, Object)}
 * (see that method's javadoc for the value contract).
 */
public interface ClassScanner {
    // ----------------------------------------------------------------------
    // Operations
    // ----------------------------------------------------------------------

    /**
     * Scans the given class file bytes, visiting the given class visitor.
     *
     * @param code The class file bytes
     * @param visitor The visitor to dispatch the class file contents to; if
     *          {@code null} this method does nothing
     */
    void scan(byte[] code, ClassVisitor visitor);
}
