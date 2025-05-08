/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
public interface AnnotationVisitor
{
    /**
     * Enters the annotation declaration.
     */
    void enterAnnotation();

    /**
     * Visits an element of the declared annotation.
     * 
     * @param name The element name
     * @param value The element value
     */
    void visitElement( String name, Object value );

    /**
     * Leaves the annotation declaration.
     */
    void leaveAnnotation();
}
