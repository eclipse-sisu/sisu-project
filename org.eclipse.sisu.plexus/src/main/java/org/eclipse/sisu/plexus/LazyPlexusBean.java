/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.util.Map.Entry;

import org.eclipse.sisu.BeanEntry;

import com.google.inject.name.Named;

/**
 * {@link Entry} representing a lazy @{@link Named} Plexus bean; the bean is only retrieved when the value is requested.
 */
final class LazyPlexusBean<T>
    implements PlexusBean<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanEntry<Named, T> bean;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LazyPlexusBean( final BeanEntry<Named, T> bean )
    {
        this.bean = bean;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getKey()
    {
        return bean.getKey().value();
    }

    public T getValue()
    {
        return bean.getValue();
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public String getDescription()
    {
        return bean.getDescription();
    }

    public Class<T> getImplementationClass()
    {
        return bean.getImplementationClass();
    }

    @Override
    public String toString()
    {
        return getKey() + "=" + getValue();
    }
}
