/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.reflect;

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
