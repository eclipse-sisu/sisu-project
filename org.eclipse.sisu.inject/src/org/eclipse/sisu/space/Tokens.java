/*******************************************************************************
 * Copyright (c) 2021-present Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.Character.isWhitespace;

/**
 * Utility methods for dealing with tokens.
 */
public final class Tokens
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Tokens()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Lazily splits the given string into whitespace-trimmed tokens, using comma as the token separator.
     *
     * @param text The text to split into tokens
     * @return Sequence of comma-separated tokens
     */
    public static Iterable<String> splitByComma( final String text )
    {
        return new Iterable<String>()
        {
            @Override
            public Iterator<String> iterator()
            {
                return new CommaSeparatedTokenIterator( text );
            }
        };
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link Iterator} that lazily splits a string into whitespace-trimmed tokens around commas.
     */
    static final class CommaSeparatedTokenIterator
        implements Iterator<String>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final String text;

        private int tokenIndex;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        CommaSeparatedTokenIterator( final String text )
        {
            this.text = text;
            this.tokenIndex = nextToken( 0 );
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public boolean hasNext()
        {
            return tokenIndex < text.length();
        }

        @Override
        public String next()
        {
            if ( hasNext() )
            {
                final int separatorIndex = nextSeparator( tokenIndex + 1 );
                final String token = text.substring( tokenIndex, trimBack( separatorIndex - 1 ) + 1 );
                tokenIndex = nextToken( separatorIndex + 1 );
                return token;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        // ----------------------------------------------------------------------
        // Implementation methods
        // ----------------------------------------------------------------------

        /**
         * Finds the start of the next token, i.e. not the separator or whitespace.
         */
        private int nextToken( final int from )
        {
            final int max = text.length();
            for ( int i = from; i < max; i++ )
            {
                final char c = text.charAt( i );
                if ( !isWhitespace( c ) && ',' != c )
                {
                    return i;
                }
            }
            return max; // return end-of-string if no more tokens
        }

        /**
         * Finds the position of the next separator that follows the current token.
         */
        private int nextSeparator( final int from )
        {
            final int max = text.length();
            for ( int i = from; i < max; i++ )
            {
                if ( ',' == text.charAt( i ) )
                {
                    return i;
                }
            }
            return max; // return end-of-string if no more separators
        }

        /**
         * Backtracks to find the non-whitespace end of the current token.
         */
        private int trimBack( final int from )
        {
            final int min = tokenIndex;
            for ( int i = from; i > min; i-- )
            {
                if ( !isWhitespace( text.charAt( i ) ) )
                {
                    return i;
                }
            }
            return min; // we know the start of the current token cannot be whitespace
        }
    }
}
