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
package org.eclipse.sisu.reflect;

import java.util.regex.Pattern;

/**
 * Enumerates various optimized filename globbing strategies.
 */
enum GlobberStrategy
{
    // ----------------------------------------------------------------------
    // Enumerated values
    // ----------------------------------------------------------------------

    ANYTHING
    {
        @Override
        final Object compile( final String glob )
        {
            return null;
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return true;
        }
    },
    SUFFIX
    {
        @Override
        final Object compile( final String glob )
        {
            return glob.substring( 1 ); // remove leading asterisk
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return filename.endsWith( (String) globPattern ); // no need for basename(...)
        }
    },
    PREFIX
    {
        @Override
        final Object compile( final String glob )
        {
            return glob.substring( 0, glob.length() - 1 ); // remove trailing asterisk
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return basename( filename ).startsWith( (String) globPattern );
        }
    },
    EXACT
    {
        @Override
        final Object compile( final String glob )
        {
            return glob;
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return globPattern.equals( basename( filename ) );
        }
    },
    PATTERN
    {
        @Override
        final Object compile( final String glob )
        {
            return Pattern.compile( "\\Q" + glob.replaceAll( "\\*+", "\\\\E.*\\\\Q" ) + "\\E" );
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return ( (Pattern) globPattern ).matcher( basename( filename ) ).matches();
        }
    };

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * Selects the optimal globber strategy for the given plain-text glob.
     * 
     * @param glob The plain-text glob
     * @return Optimal globber strategy
     */
    static final GlobberStrategy selectFor( final String glob )
    {
        if ( null == glob || "*".equals( glob ) )
        {
            return GlobberStrategy.ANYTHING;
        }
        final int firstWildcard = glob.indexOf( '*' );
        if ( firstWildcard < 0 )
        {
            return GlobberStrategy.EXACT;
        }
        final int lastWildcard = glob.lastIndexOf( '*' );
        if ( firstWildcard == lastWildcard )
        {
            if ( firstWildcard == 0 )
            {
                return GlobberStrategy.SUFFIX;
            }
            if ( lastWildcard == glob.length() - 1 )
            {
                return GlobberStrategy.PREFIX;
            }
        }
        return GlobberStrategy.PATTERN;
    }

    /**
     * Compiles the given plain-text glob into an optimized pattern.
     * 
     * @param glob The plain-text glob
     * @return Compiled glob pattern
     */
    abstract Object compile( final String glob );

    /**
     * Attempts to match the given compiled glob pattern against a filename.
     * 
     * @param globPattern The compiled glob pattern
     * @param filename The candidate filename
     * @return {@code true} if the pattern matches; otherwise {@code false}
     */
    abstract boolean matches( final Object globPattern, final String filename );

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Extracts the basename segment from the given filename.
     * 
     * @param filename The filename
     * @return Basename segment
     */
    static final String basename( final String filename )
    {
        return filename.substring( 1 + filename.lastIndexOf( '/' ) );
    }
}
