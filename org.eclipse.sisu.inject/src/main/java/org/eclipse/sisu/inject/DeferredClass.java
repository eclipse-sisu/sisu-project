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
package org.eclipse.sisu.inject;

/**
 * Placeholder {@link Class}; postpones classloading until absolutely necessary.
 */
public interface DeferredClass<T>
{
    /**
     * Retrieves the class, for example from a cache or a class loader.
     * 
     * @return Class instance
     */
    Class<T> load()
        throws TypeNotPresentException;

    /**
     * Returns the name of the deferred class.
     * 
     * @return Class name
     */
    String getName();

    /**
     * Returns a provider based on the deferred class.
     * 
     * @return Deferred provider
     */
    DeferredProvider<T> asProvider();
}
