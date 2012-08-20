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
package org.eclipse.sisu.plexus.locators;

import java.util.Map.Entry;

import org.eclipse.sisu.plexus.config.PlexusBean;
import org.eclipse.sisu.plexus.config.Roles;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

/**
 * {@link Entry} representing a missing @{@link Named} Plexus bean.
 */
final class MissingPlexusBean<T>
    implements PlexusBean<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeLiteral<T> role;

    private final String hint;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MissingPlexusBean( final TypeLiteral<T> role, final String hint )
    {
        this.role = role;
        this.hint = hint;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getKey()
    {
        return hint;
    }

    public T getValue()
    {
        return Roles.throwMissingComponentException( role, hint );
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public String getDescription()
    {
        return null;
    }

    public Class<T> getImplementationClass()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return getKey() + "=<missing>";
    }
}
