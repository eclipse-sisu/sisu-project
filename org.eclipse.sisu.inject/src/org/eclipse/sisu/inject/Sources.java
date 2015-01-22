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

import com.google.inject.Binder;
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

    // ----------------------------------------------------------------------
    // Public types
    // ----------------------------------------------------------------------

    /**
     * Binding source locations can implement this interface to hide bindings from the {@link BeanLocator}.
     * 
     * @see Binder#withSource(Object)
     */
    public interface Hidden
    {
        // marker interface
    }

    /**
     * Binding source locations can implement this interface to supply descriptions to the {@link BeanLocator}.
     * 
     * @see Binder#withSource(Object)
     */
    public interface Described
    {
        /**
         * @return Human-readable description
         */
        String getDescription();
    }

    /**
     * Binding source locations can implement this interface to supply priorities to the {@link BeanLocator}.
     * 
     * @see Binder#withSource(Object)
     */
    public interface Prioritized
    {
        /**
         * @return Priority value
         */
        int getPriority();
    }
}
