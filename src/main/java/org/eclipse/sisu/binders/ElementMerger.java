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
package org.eclipse.sisu.binders;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.sisu.reflect.Logs;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;

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
            if ( binding.acceptTargetVisitor( verifier ).booleanValue() )
            {
                localKeys.add( key );
                binding.applyTo( binder );
            }
            else
            {
                Logs.debug( "Discard binding: {}", binding, null );
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
