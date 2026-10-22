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

/**
 * Extends {@link BeanProperty} with extra methods, applicable only in case if property is queryable, like
 * for example when property is backed by a field.
 *
 * @since 1.0.1
 */
public interface BeanPropertyGetter<T> extends BeanProperty<T> {
    /**
     * Tells is the property backing field having {@code final} modifier or not. This can reveal some discrepancies in
     * legacy vs modern apps (Plexus vs JSR330), as sometimes, the fact this is a final field, but we did create
     * instance of it, may tell us that injection is not needed even if required. This is most typical for loggers, where
     * legacy/Plexus components waited for logger to be injected, while modern applications use logger factories like
     * Slf4j factory is.
     *
     * @return true if the field backing property is {@code final}.
     */
    boolean isFinal();

    /**
     * Gets the property in the given bean and returns the value.
     *
     * @param bean The bean to update
     * @return value The bean property value.
     */
    <B> T get(final B bean);
}
