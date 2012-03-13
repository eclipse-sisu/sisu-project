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

import java.util.Arrays;
import java.util.List;

import org.eclipse.sisu.locators.DefaultBeanLocator;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Child {@link WireModule} that avoids wiring dependencies that already exist in a parent {@link Injector}.
 */
public class ChildWireModule
    extends WireModule
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Injector parent;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ChildWireModule( final Injector parent, final Module... modules )
    {
        this( parent, Arrays.asList( modules ) );
    }

    public ChildWireModule( final Injector parent, final List<Module> modules )
    {
        super( modules );
        this.parent = parent;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    ElementAnalyzer getAnalyzer( final Binder binder )
    {
        // make sure we're added to locator as early as possible
        binder.requestStaticInjection( DefaultBeanLocator.class );

        // ignore any inherited bindings/dependencies
        final ElementAnalyzer analyzer = super.getAnalyzer( binder );
        for ( Injector i = parent; i != null; i = i.getParent() )
        {
            analyzer.ignoreKeys( i.getAllBindings().keySet() );
        }
        return analyzer;
    }
}
