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

import java.util.Arrays;

import org.eclipse.sisu.inject.BeanLocator;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

/**
 * Guice {@link Module} that automatically adds {@link BeanLocator}-backed bindings for unresolved dependencies.
 */
public final class WireModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final Module[] CONVERTERS = { new FileTypeConverter(), new PathTypeConverter(), new URLTypeConverter() };

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Module> modules;

    private Strategy strategy = Strategy.DEFAULT;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public WireModule( final Module... modules )
    {
        this( Arrays.asList( modules ) );
    }

    public WireModule( final Iterable<Module> modules )
    {
        this.modules = modules;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Applies a new wiring {@link Strategy} to the current module.
     * 
     * @param _strategy The new strategy
     * @return Updated module
     */
    public Module with( final Strategy _strategy )
    {
        strategy = _strategy;
        return this;
    }

    public void configure( final Binder binder )
    {
        final ElementAnalyzer analyzer = new ElementAnalyzer( binder );
        for ( final Element e : Elements.getElements( modules ) )
        {
            e.acceptVisitor( analyzer );
        }
        analyzer.apply( strategy );
    }

    // ----------------------------------------------------------------------
    // Public types
    // ----------------------------------------------------------------------

    /**
     * Wiring strategy.
     */
    public interface Strategy
    {
        /**
         * Selects the {@link Wiring} to be used for the given {@link Binder}.
         * 
         * @param binder The binder
         * @return Selected wiring
         */
        Wiring wiring( Binder binder );

        /**
         * Default wiring strategy; route all unresolved dependencies to the {@link BeanLocator}.
         */
        Strategy DEFAULT = new Strategy()
        {
            public Wiring wiring( final Binder binder )
            {
                // basic File+URL type converters
                for ( final Module m : CONVERTERS )
                {
                    m.configure( binder );
                }
                return new LocatorWiring( binder );
            }
        };
    }
}
