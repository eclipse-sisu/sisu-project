/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.inject;

import org.eclipse.sisu.Hidden;

import com.google.inject.Binding;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvidedBy;
import com.google.inject.TypeLiteral;

/**
 * Source of "implicit" bindings; includes @{@link ImplementedBy}, @{@link ProvidedBy}, and concrete types.
 */
final class ImplicitBindings
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<BindingPublisher> publishers;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ImplicitBindings( final Iterable<BindingPublisher> publishers )
    {
        this.publishers = publishers;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public <T> Binding<T> get( final TypeLiteral<T> type )
    {
        final Key implicitKey = TypeArguments.implicitKey( type.getRawType() );
        for ( final BindingPublisher p : publishers )
        {
            if ( p instanceof InjectorBindings )
            {
                // first round: check for any re-written implicit bindings
                final Injector i = ( (InjectorBindings) p ).getInjector();
                final Binding binding = i.getBindings().get( implicitKey );
                if ( null != binding )
                {
                    Logs.trace( "Using implicit binding: {} from: <>", binding, i );
                    return binding;
                }
            }
        }

        final Key justInTimeKey = Key.get( type );
        for ( final BindingPublisher p : publishers )
        {
            if ( p instanceof InjectorBindings )
            {
                // second round: fall back to just-in-time binding lookup
                final Injector i = ( (InjectorBindings) p ).getInjector();
                try
                {
                    final Binding binding = i.getBinding( justInTimeKey );
                    if ( null == Sources.getAnnotation( binding, Hidden.class ) )
                    {
                        Logs.trace( "Using just-in-time binding: {} from: <>", binding, i );
                        return binding;
                    }
                }
                catch ( final RuntimeException e )
                {
                    Logs.trace( "Problem with just-in-time binding: {}", justInTimeKey, e );
                }
                catch ( final LinkageError e )
                {
                    Logs.trace( "Problem with just-in-time binding: {}", justInTimeKey, e );
                }
            }
        }
        return null;
    }
}
