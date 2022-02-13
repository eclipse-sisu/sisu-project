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
package org.eclipse.sisu.space;

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
        final String compile( final String glob )
        {
            return null;
        }

        @Override
        final boolean matches( final String globPattern, final String filename )
        {
            return true;
        }
    },
    SUFFIX
    {
        @Override
        final String compile( final String glob )
        {
            return glob.substring( 1 ); // remove leading star
        }

        @Override
        final boolean matches( final String globPattern, final String filename )
        {
            return filename.endsWith( globPattern ); // no need for basename(...)
        }
    },
    PREFIX
    {
        @Override
        final String compile( final String glob )
        {
            return glob.substring( 0, glob.length() - 1 ); // remove trailing star
        }

        @Override
        final boolean matches( final String globPattern, final String filename )
        {
            return basename( filename ).startsWith( globPattern );
        }
    },
    EXACT
    {
        @Override
        final String compile( final String glob )
        {
            return glob;
        }

        @Override
        final boolean matches( final String globPattern, final String filename )
        {
            return globPattern.equals( basename( filename ) );
        }
    },
    PATTERN
    {
        @Override
        final String compile( final String glob )
        {
            return glob;
        }

        @Override
        final boolean matches( final String globPattern, final String filename )
        {
            final String basename = basename( filename );
            int checkIndex = 0;
            for ( final String token : Tokens.splitByStar( globPattern ) )
            {
                if ( checkIndex == 0 && globPattern.charAt( 0 ) != '*' )
                {
                    // initial match is stricter when pattern doesn't have a leading star
                    if ( !basename.startsWith( token ) )
                    {
                        return false;
                    }
                    checkIndex = token.length();
                }
                else
                {
                    // subsequent tokens must appear somewhere after the previous match
                    final int matchIndex = basename.indexOf( token, checkIndex );
                    if ( matchIndex < 0 )
                    {
                        return false;
                    }
                    checkIndex = matchIndex + token.length();
                }
            }
            // pattern matches if we've checked the entire basename or there was a trailing star
            return checkIndex == basename.length() || globPattern.charAt( globPattern.length() - 1 ) == '*';
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
    abstract String compile( final String glob );

    /**
     * Attempts to match the given compiled glob pattern against a filename.
     * 
     * @param globPattern The compiled glob pattern
     * @param filename The candidate filename
     * @return {@code true} if the pattern matches; otherwise {@code false}
     */
    abstract boolean matches( final String globPattern, final String filename );

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
