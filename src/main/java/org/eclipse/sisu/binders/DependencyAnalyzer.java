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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.sisu.reflect.DeferredProvider;
import org.eclipse.sisu.reflect.Logs;
import org.eclipse.sisu.reflect.TypeParameters;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.ProviderLookup;
import com.google.inject.spi.StaticInjectionRequest;
import com.google.inject.spi.UntargettedBinding;

/**
 * {@link BindingTargetVisitor} that collects the {@link Key}s of any injected dependencies.
 */
final class DependencyAnalyzer
    extends DefaultBindingTargetVisitor<Object, Boolean>
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        RESTRICTED_CLASSES =
            new HashSet<Class<?>>( Arrays.<Class<?>> asList( AbstractModule.class, Binder.class, Binding.class,
                                                             Injector.class, Key.class, Logger.class,
                                                             MembersInjector.class, Module.class, Provider.class,
                                                             Scope.class, TypeLiteral.class ) );
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Set<Class<?>> RESTRICTED_CLASSES;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<TypeLiteral<?>, Boolean> analyzedTypes = new HashMap<TypeLiteral<?>, Boolean>();

    private final Set<Key<?>> requiredKeys = new HashSet<Key<?>>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DependencyAnalyzer()
    {
        // properties parameter is implicitly required
        requiredKeys.add( ParameterKeys.PROPERTIES );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Set<Key<?>> findMissingKeys( final Set<Key<?>> localKeys )
    {
        final Set<Key<?>> missingKeys = new HashSet<Key<?>>();
        while ( requiredKeys.size() > 0 )
        {
            final List<Key<?>> candidateKeys = new ArrayList<Key<?>>( requiredKeys );
            requiredKeys.clear(); // reset so we can detect any implicit requirements

            for ( final Key<?> key : candidateKeys )
            {
                if ( !localKeys.contains( key ) && missingKeys.add( key ) )
                {
                    analyzeImplicitBindings( key.getTypeLiteral() );
                }
            }
        }
        return missingKeys;
    }

    @Override
    public Boolean visit( final UntargettedBinding<?> binding )
    {
        return analyzeImplementation( binding.getKey().getTypeLiteral(), true );
    }

    @Override
    public Boolean visit( final LinkedKeyBinding<?> binding )
    {
        final Key<?> linkedKey = binding.getLinkedKey();
        if ( linkedKey.getAnnotationType() == null )
        {
            return analyzeImplementation( linkedKey.getTypeLiteral(), true );
        }
        return Boolean.TRUE; // indirect binding, don't scan
    }

    @Override
    public Boolean visit( final ProviderKeyBinding<?> binding )
    {
        final Key<?> providerKey = binding.getProviderKey();
        if ( providerKey.getAnnotationType() == null )
        {
            return analyzeImplementation( providerKey.getTypeLiteral(), true );
        }
        return Boolean.TRUE; // indirect binding, don't scan
    }

    @Override
    public Boolean visit( final ProviderInstanceBinding<?> binding )
    {
        final javax.inject.Provider<?> provider = binding.getProviderInstance();
        if ( provider instanceof DeferredProvider<?> )
        {
            try
            {
                final Class<?> clazz = ( (DeferredProvider<?>) provider ).getImplementationClass().load();
                analyzeImplementation( TypeLiteral.get( clazz ), false );
            }
            catch ( final TypeNotPresentException e ) // NOPMD
            {
                // deferred provider, so we also defer any errors until someone actually tries to use it
            }
            return Boolean.TRUE;
        }
        return Boolean.valueOf( analyzeDependencies( binding.getDependencies() ) );
    }

    @Override
    public Boolean visitOther( final Binding<?> binding )
    {
        if ( binding instanceof HasDependencies )
        {
            return Boolean.valueOf( analyzeDependencies( ( (HasDependencies) binding ).getDependencies() ) );
        }
        return Boolean.TRUE;
    }

    public <T> Boolean visit( final ProviderLookup<T> lookup )
    {
        requireKey( lookup.getKey() );
        return Boolean.TRUE;
    }

    public Boolean visit( final StaticInjectionRequest request )
    {
        return Boolean.valueOf( analyzeInjectionPoints( request.getInjectionPoints() ) );
    }

    public Boolean visit( final InjectionRequest<?> request )
    {
        return Boolean.valueOf( analyzeInjectionPoints( request.getInjectionPoints() ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void requireKey( final Key<?> key )
    {
        if ( !requiredKeys.contains( key ) )
        {
            final Class<?> clazz = key.getTypeLiteral().getRawType();
            if ( javax.inject.Provider.class == clazz || com.google.inject.Provider.class == clazz )
            {
                requireKey( key.ofType( TypeParameters.get( key.getTypeLiteral(), 0 ) ) );
            }
            else if ( !RESTRICTED_CLASSES.contains( clazz ) )
            {
                requiredKeys.add( key );
            }
        }
    }

    private Boolean analyzeImplementation( final TypeLiteral<?> type, final boolean reportErrors )
    {
        Boolean applyBinding = analyzedTypes.get( type );
        if ( null == applyBinding )
        {
            applyBinding = Boolean.TRUE;
            if ( TypeParameters.isConcrete( type ) && !type.toString().startsWith( "java" ) )
            {
                try
                {
                    // check methods+fields first and avoid short-circuiting to maximize dependency analysis results
                    final boolean rhs = analyzeInjectionPoints( InjectionPoint.forInstanceMethodsAndFields( type ) );
                    if ( !analyzeDependencies( InjectionPoint.forConstructorOf( type ).getDependencies() ) || !rhs )
                    {
                        applyBinding = Boolean.FALSE;
                    }
                }
                catch ( final RuntimeException e )
                {
                    if ( reportErrors )
                    {
                        Logs.debug( "Potential problem: {}", type, e );
                    }
                    applyBinding = Boolean.FALSE;
                }
                catch ( final LinkageError e )
                {
                    if ( reportErrors )
                    {
                        Logs.debug( "Potential problem: {}", type, e );
                    }
                    applyBinding = Boolean.FALSE;
                }
            }
            analyzedTypes.put( type, applyBinding );
        }
        return applyBinding;
    }

    private boolean analyzeInjectionPoints( final Set<InjectionPoint> points )
    {
        boolean applyBinding = true;
        for ( final InjectionPoint p : points )
        {
            applyBinding &= analyzeDependencies( p.getDependencies() );
        }
        return applyBinding;
    }

    private boolean analyzeDependencies( final Collection<Dependency<?>> dependencies )
    {
        boolean applyBinding = true;
        for ( final Dependency<?> d : dependencies )
        {
            final Key<?> key = d.getKey();
            if ( key.hasAttributes() && "Assisted".equals( key.getAnnotationType().getSimpleName() ) )
            {
                applyBinding = false; // avoid directly binding AssistedInject based components
            }
            else
            {
                requireKey( key );
            }
        }
        return applyBinding;
    }

    private void analyzeImplicitBindings( final TypeLiteral<?> type )
    {
        if ( !analyzedTypes.containsKey( type ) )
        {
            final Class<?> clazz = type.getRawType();
            if ( TypeParameters.isConcrete( clazz ) )
            {
                analyzeImplementation( type, false );
            }
            else
            {
                analyzedTypes.put( type, Boolean.TRUE );
                final ImplementedBy implementedBy = clazz.getAnnotation( ImplementedBy.class );
                if ( null != implementedBy )
                {
                    requireKey( Key.get( implementedBy.value() ) );
                }
                else
                {
                    final ProvidedBy providedBy = clazz.getAnnotation( ProvidedBy.class );
                    if ( null != providedBy )
                    {
                        requireKey( Key.get( providedBy.value() ) );
                    }
                }
            }
        }
    }
}
