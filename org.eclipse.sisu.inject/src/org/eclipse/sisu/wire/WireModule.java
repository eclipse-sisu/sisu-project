/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.wire;

import java.util.Arrays;

import org.eclipse.sisu.inject.BeanLocator;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

/**
 * Guice {@link Module} that automatically adds {@link BeanLocator}-backed bindings for non-local bean dependencies.
 */
public final class WireModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final Module[] CONVERTERS = { new FileTypeConverter(), new URLTypeConverter() };

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
        analyzer.apply( strategy.wiring( binder ) );
    }

    // ----------------------------------------------------------------------
    // Public types
    // ----------------------------------------------------------------------

    public interface Strategy
    {
        Wiring wiring( Binder binder );

        Strategy DEFAULT = new Strategy()
        {
            public Wiring wiring( final Binder binder )
            {
                for ( final Module m : CONVERTERS )
                {
                    m.configure( binder );
                }
                return new LocatorWiring( binder );
            }
        };
    }
}
