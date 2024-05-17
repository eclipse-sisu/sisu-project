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

import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Constants and utility methods for dealing with Plexus hints.
 */
public final class Hints
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    public static final String[] NO_HINTS = {};

    public static final String DEFAULT_HINT = "default";

    private static final List<String> NO_HINTS_LIST = Arrays.asList( NO_HINTS );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Hints()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Returns the canonical form of the given Plexus hint.
     * 
     * @param hint The Plexus hint
     * @return Canonical hint denoting the same component as the given hint
     */
    public static String canonicalHint( final String hint )
    {
        // interning hints is a good idea because there's a lot of duplication
        return null == hint || hint.length() == 0 ? DEFAULT_HINT : hint.intern();
    }

    /**
     * Determines if the given Plexus hint denotes the default component.
     * 
     * @param hint The Plexus hint
     * @return {@code true} if the given hint denotes the default component
     */
    public static boolean isDefaultHint( final String hint )
    {
        return DEFAULT_HINT.equals( hint ) || null == hint || hint.length() == 0;
    }

    /**
     * Returns the canonical form of the given Plexus hints.
     * 
     * @param hints The Plexus hints
     * @return Array of canonical hints
     */
    public static String[] canonicalHints( final String... hints )
    {
        final int length = hints.length;
        if ( length == 0 || length == 1 && hints[0].length() == 0 )
        {
            return NO_HINTS;
        }
        for ( int i = 0; i < length; i++ )
        {
            hints[i] = canonicalHint( hints[i] );
        }
        return hints;
    }

    /**
     * Returns the canonical form of the given Plexus hints.
     * 
     * @param hints The Plexus hints
     * @return List of canonical hints
     */
    public static List<String> canonicalHints( final List<String> hints )
    {
        final int length = hints.size();
        if ( length == 0 || length == 1 && hints.get( 0 ).length() == 0 )
        {
            return NO_HINTS_LIST;
        }
        for ( int i = 0; i < length; i++ )
        {
            hints.set( i, canonicalHint( hints.get( i ) ) );
        }
        return hints;
    }

    /**
     * Returns the Plexus hints contained in the given @{@link Requirement}.
     * 
     * @param requirement The Plexus requirement
     * @return Array of canonical hints
     */
    public static String[] canonicalHints( final Requirement requirement )
    {
        final String[] hints = requirement.hints();
        if ( hints.length > 0 )
        {
            return canonicalHints( hints );
        }
        final String hint = requirement.hint();
        if ( hint.length() > 0 )
        {
            return canonicalHints( hint );
        }
        return NO_HINTS;
    }
}
