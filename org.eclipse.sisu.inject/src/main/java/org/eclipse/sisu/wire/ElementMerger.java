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
package org.eclipse.sisu.wire;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.sisu.inject.Logs;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;

/**
 * {@link ElementVisitor} that verifies {@link Binding}s and merges any duplicates.
 */
final class ElementMerger
    extends DefaultElementVisitor<Void>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final DependencyVerifier verifier = new DependencyVerifier();

    private final Set<Key<?>> localKeys = new HashSet<Key<?>>();

    private final Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ElementMerger( final Binder binder )
    {
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public <T> Void visit( final Binding<T> binding )
    {
        final Key<T> key = binding.getKey();
        if ( !localKeys.contains( key ) )
        {
            if ( Boolean.TRUE.equals( binding.acceptTargetVisitor( verifier ) ) )
            {
                localKeys.add( key );
                binding.applyTo( binder );
            }
            else
            {
                Logs.trace( "Discard binding: {}", binding, null );
            }
        }
        return null;
    }

    @Override
    public Void visitOther( final Element element )
    {
        element.applyTo( binder );
        return null;
    }
}
