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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

/**
 * Guice {@link Module} that discards any duplicate or broken bindings.
 */
public final class MergedModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Module> modules;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public MergedModule( final Module... modules )
    {
        this.modules = Arrays.asList( modules );
    }

    public MergedModule( final Iterable<Module> modules )
    {
        this.modules = modules;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        final ElementMerger merger = new ElementMerger( binder );
        for ( final Element e : Elements.getElements( modules ) )
        {
            e.acceptVisitor( merger );
        }
    }
}
