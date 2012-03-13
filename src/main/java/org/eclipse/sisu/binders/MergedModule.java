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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

public final class MergedModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Module> modules;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public MergedModule( final Module... modules )
    {
        this.modules = Arrays.asList( modules );
    }

    public MergedModule( final List<Module> modules )
    {
        this.modules = modules;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        final ElementMerger merger = new ElementMerger( binder );
        for ( final Module m : modules )
        {
            for ( final Element e : Elements.getElements( m ) )
            {
                e.acceptVisitor( merger );
            }
        }
    }
}
