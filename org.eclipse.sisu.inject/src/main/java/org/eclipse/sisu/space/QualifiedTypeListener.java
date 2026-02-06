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

import com.google.inject.Binder;
import javax.inject.Qualifier;

/**
 * Listens for types annotated with {@link Qualifier} annotations.
 */
public interface QualifiedTypeListener {
    /**
     * Invoked when the {@link QualifiedTypeVisitor} finds a qualified type.
     *
     * @param qualifiedType The qualified type
     * @param source The source of this type
     * @see Binder#withSource(Object)
     */
    void hear(Class<?> qualifiedType, Object source);
}
