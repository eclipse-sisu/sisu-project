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
package org.eclipse.sisu.locators;

import java.util.List;
import java.util.Map;

import org.eclipse.sisu.locators.spi.BindingPublisher;
import org.eclipse.sisu.locators.spi.BindingSubscriber;
import org.eclipse.sisu.reflect.Logs;
import org.eclipse.sisu.reflect.TypeParameters;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Publisher of {@link Binding}s from a single {@link Injector}; ranked according to a given {@link RankingFunction}.
 */
final class InjectorPublisher
    implements BindingPublisher
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final TypeLiteral<?> OBJECT_TYPE_LITERAL = TypeLiteral.get( Object.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Injector injector;

    private final RankingFunction function;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    InjectorPublisher( final Injector injector, final RankingFunction function )
    {
        this.injector = injector;
        this.function = function;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Injector getInjector()
    {
        return injector;
    }

    public <T> void subscribe( final BindingSubscriber<T> subscriber )
    {
        final TypeLiteral<T> type = subscriber.type();
        publishBindings( type, subscriber, null );
        final Class<?> clazz = type.getRawType();
        if ( clazz != type.getType() )
        {
            publishBindings( TypeLiteral.get( clazz ), subscriber, type );
        }
        if ( clazz != Object.class )
        {
            publishBindings( OBJECT_TYPE_LITERAL, subscriber, type );
        }
    }

    public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
    {
        final Map<Key<?>, ?> ourBindings = injector.getBindings();
        for ( final Binding<T> binding : subscriber.bindings() )
        {
            if ( binding == ourBindings.get( binding.getKey() ) )
            {
                subscriber.remove( binding );
            }
        }
    }

    @Override
    public int hashCode()
    {
        return injector.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof InjectorPublisher )
        {
            return injector.equals( ( (InjectorPublisher) rhs ).injector );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return Logs.toString( injector );
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    static boolean isVisible( final Binding<?> binding )
    {
        return false == binding.getSource() instanceof HiddenBinding;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static boolean isAssignableFrom( final TypeLiteral<?> superType, final Binding<?> binding )
    {
        // don't match the exact implementation as it's already covered by an explicit binding
        final Class<?> implementation = binding.acceptTargetVisitor( ImplementationVisitor.THIS );
        if ( null != implementation && superType.getRawType() != implementation )
        {
            return TypeParameters.isAssignableFrom( superType, TypeLiteral.get( implementation ) );
        }
        return false;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private void publishBindings( final TypeLiteral searchType, final BindingSubscriber subscriber,
                                  final TypeLiteral superType )
    {
        final List<Binding<?>> bindings = injector.findBindingsByType( searchType );
        for ( int i = 0, size = bindings.size(); i < size; i++ )
        {
            final Binding binding = bindings.get( i );
            if ( isVisible( binding ) && ( null == superType || isAssignableFrom( superType, binding ) ) )
            {
                subscriber.add( binding, function.rank( binding ) );
            }
        }
    }
}
