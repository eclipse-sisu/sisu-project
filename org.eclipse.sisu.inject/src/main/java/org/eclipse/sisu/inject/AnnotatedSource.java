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

import com.google.inject.Binder;
import com.google.inject.Binding;
import java.lang.annotation.Annotation;

/**
 * Binding source locations can implement this interface to supply annotations to the {@link BeanLocator}.
 *
 * @see Binder#withSource(Object)
 */
public interface AnnotatedSource {
    /**
     * Returns the annotation of the given type that is associated with the binding.
     *
     * @param binding The binding
     * @param annotationType The annotation type
     * @return The associated annotation; {@code null} if no such annotation exists
     */
    <T extends Annotation> T getAnnotation(Binding<?> binding, Class<T> annotationType);
}
