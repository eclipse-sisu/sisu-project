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

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.inject.Provider;

import com.google.inject.Binding;
import com.google.inject.Scopes;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProvidesMethodBinding;

/**
 * Utility methods for dealing with changes in the Guice 4.0 SPI.
 */
public final class Guice4
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean hasDeclaringSource;
        try
        {
            // in Guice4 binding.getSource() returns ElementSource and not the original declaring source
            hasDeclaringSource = com.google.inject.spi.ElementSource.class.getMethod( "getDeclaringSource" ) != null; // NOSONAR
        }
        catch ( final Exception e )
        {
            hasDeclaringSource = false;
        }
        catch ( final LinkageError e )
        {
            hasDeclaringSource = false;
        }
        HAS_DECLARING_SOURCE = hasDeclaringSource;

        boolean hasUserSuppliedProvider;
        try
        {
            // in Guice4 getProviderInstance() is deprecated in favour of getUserSuppliedProvider()
            hasUserSuppliedProvider = ProviderInstanceBinding.class.getMethod( "getUserSuppliedProvider" ) != null; // NOSONAR
        }
        catch ( final Exception e )
        {
            hasUserSuppliedProvider = false;
        }
        catch ( final LinkageError e )
        {
            hasUserSuppliedProvider = false;
        }
        HAS_USER_SUPPLIED_PROVIDER = hasUserSuppliedProvider;

        boolean hasOldScopesSingleton;
        try
        {
            // in Guice4 using Scopes.SINGLETON.scope(...) outside of the Injector is deprecated
            hasOldScopesSingleton = Scopes.class.equals( Scopes.SINGLETON.getClass().getEnclosingClass() ) //
                && Scopes.SINGLETON.scope( null /* key */, null /* provider */ ) != null;
        }
        catch ( final Exception e )
        {
            hasOldScopesSingleton = false;
        }
        catch ( final LinkageError e )
        {
            hasOldScopesSingleton = false;
        }
        HAS_OLD_SCOPES_SINGLETON = hasOldScopesSingleton;

        boolean hasProvidesMethodSpi;
        try
        {
            // in Guice4 @Provides Method providers implement ProvidesMethodBinding
            hasProvidesMethodSpi = ProvidesMethodBinding.class instanceof Class<?>;
        }
        catch ( final Exception e )
        {
            hasProvidesMethodSpi = false;
        }
        catch ( final LinkageError e )
        {
            hasProvidesMethodSpi = false;
        }
        HAS_PROVIDES_METHOD_SPI = hasProvidesMethodSpi;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean HAS_DECLARING_SOURCE;

    private static final boolean HAS_USER_SUPPLIED_PROVIDER;

    private static final boolean HAS_OLD_SCOPES_SINGLETON;

    private static final boolean HAS_PROVIDES_METHOD_SPI;

    static final Object NIL = new Object();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Guice4()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Returns the source that originally declared the given binding.
     * 
     * @param binding The binding
     * @return Declaring source; {@code null} if it doesn't exist
     */
    public static Object getDeclaringSource( final Binding<?> binding )
    {
        final Object source = binding.getSource();
        if ( HAS_DECLARING_SOURCE && source instanceof com.google.inject.spi.ElementSource )
        {
            return ( (com.google.inject.spi.ElementSource) source ).getDeclaringSource();
        }
        return source;
    }

    /**
     * Returns the provider that originally backed the given binding.
     * 
     * @param binding The binding
     * @return Provider instance
     */
    @SuppressWarnings( "deprecation" )
    public static Provider<?> getProviderInstance( final ProviderInstanceBinding<?> binding )
    {
        return HAS_USER_SUPPLIED_PROVIDER ? binding.getUserSuppliedProvider() : binding.getProviderInstance();
    }

    /**
     * Attempts to invoke the given binding statically; returns {@code null} if the binding isn't static.
     *
     * @param binding The binding
     * @return Statically bound instance
     */
    public static Object invokeStaticBinding( final Binding<?> binding )
    {
        if ( binding instanceof InstanceBinding<?> )
        {
            return ( (InstanceBinding<?>) binding ).getInstance();
        }
        if ( binding instanceof ProviderInstanceBinding<?> )
        {
            final Provider<?> provider = getProviderInstance( (ProviderInstanceBinding<?>) binding );
            if ( HAS_PROVIDES_METHOD_SPI && provider instanceof ProvidesMethodBinding )
            {
                try
                {
                    // attempt to invoke the @Provides Method statically via reflection
                    final ProvidesMethodBinding providesMethod = (ProvidesMethodBinding) provider;
                    if ( !providesMethod.getMethod().isAccessible() )
                    {
                        AccessController.doPrivileged( new PrivilegedAction<Void>()
                        {
                            public Void run()
                            {
                                providesMethod.getMethod().setAccessible( true );
                                return null;
                            }
                        } );
                    }
                    return providesMethod.getMethod().invoke( providesMethod.getEnclosingInstance() );
                }
                catch ( final Exception e )
                {
                    return null;
                }
                catch ( final LinkageError e )
                {
                    return null;
                }
            }
            return provider.get();
        }
        return null;
    }

    /**
     * Returns a lazy provider that only uses the binding once and caches the result.
     * 
     * @param binding The binding
     * @return Lazy caching provider
     */
    @SuppressWarnings( "unchecked" )
    public static <T> Provider<T> lazy( final Binding<T> binding )
    {
        if ( HAS_OLD_SCOPES_SINGLETON )
        {
            // avoids introducing extra locks, but won't be supported going forwards
            return Scopes.SINGLETON.scope( binding.getKey(), binding.getProvider() );
        }

        // future behaviour: lazy holder with its own lock
        final Provider<T> provider = binding.getProvider();
        return new Provider<T>()
        {
            private volatile Object value = NIL; // NOSONAR

            public T get()
            {
                if ( NIL == value )
                {
                    synchronized ( this )
                    {
                        if ( NIL == value )
                        {
                            value = provider.get();
                        }
                    }
                }
                return (T) value;
            }
        };
    }
}
