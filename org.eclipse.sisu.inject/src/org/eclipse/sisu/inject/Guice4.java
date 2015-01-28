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

import javax.inject.Provider;

import com.google.inject.Binding;
import com.google.inject.Scopes;
import com.google.inject.spi.ProviderInstanceBinding;

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
            // support future where binding.getSource() returns ElementSource and not the original declaring source
            hasDeclaringSource = com.google.inject.spi.ElementSource.class.getMethod( "getDeclaringSource" ) != null;
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
            // support future where getProviderInstance() is deprecated in favour of getUserSuppliedProvider()
            hasUserSuppliedProvider = ProviderInstanceBinding.class.getMethod( "getUserSuppliedProvider" ) != null;
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

        boolean hasLazyScopesSingleton;
        try
        {
            // detect future where applying this scope outside of the injector throws OutOfScopeException
            hasLazyScopesSingleton = Scopes.SINGLETON.scope( null /* key */, null /* provider */) != null;
        }
        catch ( final Exception e )
        {
            hasLazyScopesSingleton = false;
        }
        catch ( final LinkageError e )
        {
            hasLazyScopesSingleton = false;
        }
        HAS_LAZY_SCOPES_SINGLETON = hasLazyScopesSingleton;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean HAS_DECLARING_SOURCE;

    private static final boolean HAS_USER_SUPPLIED_PROVIDER;

    private static final boolean HAS_LAZY_SCOPES_SINGLETON;

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
     * Returns a lazy provider that only uses the binding once and caches the result.
     * 
     * @param binding The binding
     * @return Lazy provider
     */
    @SuppressWarnings( "unchecked" )
    public static <T> Provider<T> getLazyProvider( final Binding<T> binding )
    {
        if ( HAS_LAZY_SCOPES_SINGLETON )
        {
            // avoids introducing extra locks, but won't be supported going forwards
            return Scopes.SINGLETON.scope( binding.getKey(), binding.getProvider() );
        }
        // future behaviour: lazy holder with its own lock
        final Provider<T> provider = binding.getProvider();
        return new Provider<T>()
        {
            private volatile Object value = NIL;

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
