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

import org.eclipse.sisu.inject.DeferredClass;

/**
 * Pseudo {@link DeferredClass} backed by an already loaded {@link Class}.
 */
public final class LoadedClass<T>
    extends AbstractDeferredClass<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<T> clazz;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public LoadedClass( final Class<? extends T> clazz )
    {
        this.clazz = (Class<T>) clazz;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<T> load()
    {
        return clazz;
    }

    public String getName()
    {
        return clazz.getName();
    }

    @Override
    public int hashCode()
    {
        return clazz.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof LoadedClass<?> )
        {
            return clazz == ( (LoadedClass<?>) rhs ).clazz;
        }
        return false;
    }

    @Override
    public String toString()
    {
        final String id = "Loaded " + clazz;
        final ClassLoader space = clazz.getClassLoader();
        return null != space ? id + " from " + space : id;
    }
}
