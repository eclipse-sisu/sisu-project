/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.CloningClassSpace;

/**
 * Enhanced Plexus component map with additional book-keeping.
 */
final class PlexusTypeRegistry
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Component LOAD_ON_START_PLACEHOLDER =
        new ComponentImpl( Object.class, "", Strategies.LOAD_ON_START, "" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<String, Component> components = new HashMap<String, Component>();

    private final Map<String, DeferredClass<?>> implementations = new HashMap<String, DeferredClass<?>>();

    private final Set<String> deferredNames = new HashSet<String>();

    private final ClassSpace space;

    private CloningClassSpace clones;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusTypeRegistry( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    /**
     * @return Current class space
     */
    ClassSpace getSpace()
    {
        return space;
    }

    /**
     * Records that the given Plexus component should be loaded when the container starts.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     */
    void loadOnStart( final String role, final String hint )
    {
        final String key = Roles.canonicalRoleHint( role, hint );
        final Component c = components.get( key );
        if ( null == c )
        {
            components.put( key, LOAD_ON_START_PLACEHOLDER );
        }
        else if ( !Strategies.LOAD_ON_START.equals( c.instantiationStrategy() ) )
        {
            components.put( key, new ComponentImpl( c.role(), c.hint(), Strategies.LOAD_ON_START, c.description() ) );
        }
    }

    /**
     * Registers the given component, automatically disambiguating between implementations bound multiple times.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @param instantiationStrategy The instantiation strategy
     * @param description The component description
     * @param implementation The implementation
     * @return The implementation the component was successfully registered with; otherwise {@code null}
     */
    String addComponent( final String role, final String hint, final String instantiationStrategy,
                         final String description, final String implementation )
    {
        final Class<?> roleType = loadRole( role, implementation );
        if ( null == roleType )
        {
            return null;
        }

        final String canonicalHint = Hints.canonicalHint( hint );
        final String key = Roles.canonicalRoleHint( role, canonicalHint );

        /*
         * COMPONENT...
         */
        final Component oldComponent = components.get( key );
        if ( null == oldComponent )
        {
            components.put( key, new ComponentImpl( roleType, canonicalHint, instantiationStrategy, description ) );
        }
        else if ( LOAD_ON_START_PLACEHOLDER == oldComponent )
        {
            components.put( key, new ComponentImpl( roleType, canonicalHint, Strategies.LOAD_ON_START, description ) );
        }

        /*
         * ...IMPLEMENTATION
         */
        DeferredClass<?> implementationType = implementations.get( key );
        if ( null == implementationType )
        {
            if ( deferredNames.add( implementation ) )
            {
                implementationType = space.deferLoadClass( implementation );
            }
            else
            {
                // type already used for another role, so we must clone it
                implementationType = cloneImplementation( implementation );
            }
            implementations.put( key, implementationType );
            return implementationType.getName();
        }
        final String oldImplementation = implementationType.getName();
        if ( implementation.equals( CloningClassSpace.originalName( oldImplementation ) ) )
        {
            return oldImplementation; // merge configuration
        }

        Logs.trace( "Duplicate implementations for Plexus role: {}", key, null );
        Logs.trace( "Using: {} ignoring: {}", oldImplementation, implementation );

        return null;
    }

    /**
     * @return Plexus component map
     */
    Map<Component, DeferredClass<?>> getComponents()
    {
        final Map<Component, DeferredClass<?>> map = new HashMap<Component, DeferredClass<?>>();
        for ( final Entry<String, DeferredClass<?>> i : implementations.entrySet() )
        {
            map.put( components.get( i.getKey() ), i.getValue() );
        }
        return map;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to load the given Plexus role, checks constructors for concrete types.
     * 
     * @param role The Plexus role
     * @param implementation The implementation
     * @return Loaded Plexus role
     */
    private Class<?> loadRole( final String role, final String implementation )
    {
        try
        {
            return space.loadClass( role );
        }
        catch ( final TypeNotPresentException e )
        {
            Logs.trace( "Ignoring Plexus role: {}", role, e );
        }
        return null;
    }

    /**
     * Clones an implementation so it can be bound again with different configuration.
     * 
     * @param implementation The implementation
     * @return Cloned implementation
     */
    private DeferredClass<?> cloneImplementation( final String implementation )
    {
        if ( null == clones )
        {
            clones = new CloningClassSpace( space );
        }
        return clones.cloneClass( implementation );
    }
}
