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
package org.eclipse.sisu.plexus;

import java.util.Map.Entry;

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
