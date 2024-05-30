/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
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

import java.util.List;

import com.google.inject.MembersInjector;

/**
 * {@link MembersInjector} that takes {@link PropertyBinding}s and applies them to bean instances.
 */
final class BeanInjector<B>
    implements MembersInjector<B>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PropertyBinding[] bindings;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanInjector( final List<PropertyBinding> bindings )
    {
        final int size = bindings.size();
        this.bindings = new PropertyBinding[size];
        for ( int i = 0, n = size; i < size; )
        {
            // reverse: inject superclass before sub
            this.bindings[i++] = bindings.get( --n );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void injectMembers( final B bean )
    {
        for ( final PropertyBinding b : bindings )
        {
            b.injectProperty( bean );
        }
    }
}
