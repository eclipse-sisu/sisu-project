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
package org.eclipse.sisu.inject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.sisu.Hidden;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

/**
 * Publisher of {@link Binding}s from a single {@link Injector}; ranked according to a given {@link RankingFunction}.
 */
@Singleton
public final class InjectorBindings
    implements BindingPublisher
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Key<BindingPublisher> BINDING_PUBLISHER_KEY = Key.get( BindingPublisher.class );

    private static final Key<RankingFunction> RANKING_FUNCTION_KEY = Key.get( RankingFunction.class );

    private static final RankingFunction DEFAULT_RANKING_FUNCTION = new DefaultRankingFunction();

    private static final TypeLiteral<Object> OBJECT_TYPE_LITERAL = TypeLiteral.get( Object.class );

    private static final Binding<?>[] NO_BINDINGS = {};

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Injector injector;

    private final RankingFunction function;

    private volatile Binding<?>[] wildcards; // NOSONAR

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public InjectorBindings( final Injector injector, final RankingFunction function )
    {
        this.injector = injector;
        this.function = function;
    }

    @Inject
    public InjectorBindings( final Injector injector )
    {
        this( injector, findRankingFunction( injector ) );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public static BindingPublisher findBindingPublisher( final Injector injector )
    {
        // check the injector's local explicit bindings first for custom publisher
        Binding<?> binding = injector.getBindings().get( BINDING_PUBLISHER_KEY );
        if ( null != binding )
        {
            return (BindingPublisher) binding.getProvider().get();
        }
        // otherwise check any parents for an explicit binding to use as a template
        binding = findExplicitBinding( injector.getParent(), BINDING_PUBLISHER_KEY );
        if ( null != binding )
        {
            final Class<?> impl = Implementations.find( binding );
            if ( null != impl )
            {
                try
                {
                    // create a new instance from the parent template, this time using the current injector
                    return (BindingPublisher) impl.getConstructor( Injector.class ).newInstance( injector );
                }
                catch ( final Exception e )
                {
                    final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
                    Logs.debug( "Problem creating: {}", impl, cause );
                }
                catch ( final LinkageError e )
                {
                    Logs.debug( "Problem creating: {}", impl, e );
                }
            }
        }
        // fall back to default implementation
        return new InjectorBindings( injector );
    }

    public static RankingFunction findRankingFunction( final Injector injector )
    {
        final Binding<RankingFunction> binding = findExplicitBinding( injector, RANKING_FUNCTION_KEY );
        return null != binding ? binding.getProvider().get() : DEFAULT_RANKING_FUNCTION;
    }

    public <T> void subscribe( final BindingSubscriber<T> subscriber )
    {
        final TypeLiteral<T> type = subscriber.type();
        final Class<?> clazz = type.getRawType();

        if ( clazz != Object.class )
        {
            publishExactMatches( type, subscriber );
            if ( clazz != type.getType() )
            {
                publishGenericMatches( type, subscriber, clazz );
            }
        }

        publishWildcardMatches( type, subscriber );
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

    public int maxBindingRank()
    {
        return function.maxRank();
    }

    @SuppressWarnings( "unchecked" )
    public <T> T adapt( final Class<T> type )
    {
        return Injector.class == type ? (T) injector : null;
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
        if ( rhs instanceof InjectorBindings )
        {
            return injector.equals( ( (InjectorBindings) rhs ).injector );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return Logs.toString( injector );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Searches {@link Injector} and its parents for an explicit binding of the given {@link Key}.
     * 
     * @param injector The injector
     * @param key The binding key
     * @return Explicit binding of the key; {@code null} if it doesn't exist
     */
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private static <T> Binding<T> findExplicitBinding( final Injector injector, final Key<T> key )
    {
        Binding binding = null;
        for ( Injector i = injector; i != null; i = i.getParent() )
        {
            binding = i.getBindings().get( key );
            if ( binding != null )
            {
                break;
            }
        }
        return binding;
    }

    private static <T, S> boolean isAssignableFrom( final TypeLiteral<T> type, final Binding<S> binding )
    {
        final Class<?> implementation = Implementations.find( binding );
        if ( null != implementation && type.getRawType() != implementation )
        {
            return TypeArguments.isAssignableFrom( type, TypeLiteral.get( implementation ) );
        }
        // either the implementation couldn't be deduced or we're looking up the exact implementation;
        // Guice includes an untargeted binding for the implementation which will have been reported
        // by publishExactMatches, so ignore generic/wildcard matches here to avoid duplicate results.
        return false;
    }

    private <T> void publishExactMatches( final TypeLiteral<T> type, final BindingSubscriber<T> subscriber )
    {
        final List<Binding<T>> bindings = injector.findBindingsByType( type );
        for ( int i = 0, size = bindings.size(); i < size; i++ )
        {
            final Binding<T> binding = bindings.get( i );
            if ( null == Sources.getAnnotation( binding, Hidden.class ) )
            {
                subscriber.add( binding, function.rank( binding ) );
            }
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private <T, S> void publishGenericMatches( final TypeLiteral<T> type, final BindingSubscriber<T> subscriber,
                                               final Class<S> rawType )
    {
        final List<Binding<S>> bindings = injector.findBindingsByType( TypeLiteral.get( rawType ) );
        for ( int i = 0, size = bindings.size(); i < size; i++ )
        {
            final Binding binding = bindings.get( i );
            if ( null == Sources.getAnnotation( binding, Hidden.class ) && isAssignableFrom( type, binding ) )
            {
                subscriber.add( binding, function.rank( binding ) );
            }
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private <T> void publishWildcardMatches( final TypeLiteral<T> type, final BindingSubscriber<T> subscriber )
    {
        final boolean untyped = type.getRawType() == Object.class;
        for ( final Binding binding : getWildcardBindings() )
        {
            if ( untyped || isAssignableFrom( type, binding ) )
            {
                subscriber.add( binding, function.rank( binding ) );
            }
        }
    }

    private Binding<?>[] getWildcardBindings()
    {
        if ( null == wildcards )
        {
            synchronized ( this )
            {
                if ( null == wildcards )
                {
                    final List<Binding<?>> visible = new ArrayList<Binding<?>>();
                    final List<Binding<Object>> candidates = injector.findBindingsByType( OBJECT_TYPE_LITERAL );
                    for ( int i = 0, size = candidates.size(); i < size; i++ )
                    {
                        final Binding<?> binding = candidates.get( i );
                        if ( null == Sources.getAnnotation( binding, Hidden.class ) )
                        {
                            visible.add( binding );
                        }
                    }
                    wildcards = visible.isEmpty() ? NO_BINDINGS : visible.toArray( new Binding[visible.size()] );
                }
            }
        }
        return wildcards;
    }
}
