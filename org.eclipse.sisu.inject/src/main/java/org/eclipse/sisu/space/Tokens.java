/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
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
package org.eclipse.sisu.space;

import static java.lang.Character.isWhitespace;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Utility methods for dealing with tokens.
 */
public final class Tokens {
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Tokens() {
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
    public static Iterable<String> splitByComma(final String text) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new TokenIterator(text, ',', true);
            }
        };
    }

    /**
     * Lazily splits the given string into non-trimmed tokens, using star as the token separator.
     *
     * @param text The text to split into tokens
     * @return Sequence of star-separated tokens
     */
    public static Iterable<String> splitByStar(final String text) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new TokenIterator(text, '*', false);
            }
        };
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link Iterator} that lazily splits a string into tokens.
     */
    static final class TokenIterator implements Iterator<String> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final String text;

        private final char separator;

        private final boolean trimming;

        private int tokenIndex;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        TokenIterator(final String text, final char separator, final boolean trimming) {
            this.text = text;
            this.separator = separator;
            this.trimming = trimming;
            this.tokenIndex = nextToken(0);
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public boolean hasNext() {
            return tokenIndex < text.length();
        }

        @Override
        public String next() {
            if (hasNext()) {
                final int separatorIndex = nextSeparator(tokenIndex + 1);
                final int tokenEnd = trimming ? trimBack(separatorIndex - 1) + 1 : separatorIndex;
                final String token = text.substring(tokenIndex, tokenEnd);
                tokenIndex = nextToken(separatorIndex + 1);
                return token;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        // ----------------------------------------------------------------------
        // Implementation methods
        // ----------------------------------------------------------------------

        /**
         * Finds the start of the next token, i.e. not the separator or whitespace when trimming.
         */
        private int nextToken(final int from) {
            final int max = text.length();
            for (int i = from; i < max; i++) {
                final char c = text.charAt(i);
                if (c != separator && false == (trimming && isWhitespace(c))) {
                    return i;
                }
            }
            return max; // return end-of-string if no more tokens
        }

        /**
         * Finds the position of the next separator that follows the current token.
         */
        private int nextSeparator(final int from) {
            final int i = text.indexOf(separator, from);
            return i >= 0 ? i : text.length();
        }

        /**
         * Backtracks to find the non-whitespace end of the current token.
         */
        private int trimBack(final int from) {
            final int min = tokenIndex;
            for (int i = from; i > min; i--) {
                if (!isWhitespace(text.charAt(i))) {
                    return i;
                }
            }
            return min; // we know the start of the current token cannot be whitespace
        }
    }
}
