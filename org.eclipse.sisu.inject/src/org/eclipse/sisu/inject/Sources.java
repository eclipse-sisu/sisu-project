/*******************************************************************************
 * Copyright (c) 2010, 2014 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.inject;

import com.google.inject.Binding;

/**
 * Utility methods for dealing with annotated sources.
 */
public final class Sources
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
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean HAS_DECLARING_SOURCE;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Sources()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static Object getDeclaringSource( final Binding<?> binding )
    {
        final Object source = binding.getSource();
        if ( HAS_DECLARING_SOURCE && source instanceof com.google.inject.spi.ElementSource )
        {
            return ( (com.google.inject.spi.ElementSource) source ).getDeclaringSource();
        }
        return source;
    }
}
