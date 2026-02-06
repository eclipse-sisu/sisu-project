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

/**
 * Enumerates various optimized globbing strategies.
 */
public enum GlobberStrategy {
    // ----------------------------------------------------------------------
    // Enumerated values
    // ----------------------------------------------------------------------

    ANYTHING {
        @Override
        public final String compile(final String glob) {
            return null;
        }

        @Override
        public final boolean matches(final String globPattern, final String name) {
            return true;
        }
    },
    SUFFIX {
        @Override
        public final String compile(final String glob) {
            return glob.substring(1); // remove leading star
        }

        @Override
        public final boolean matches(final String globPattern, final String name) {
            return name.endsWith(globPattern);
        }
    },
    PREFIX {
        @Override
        public final String compile(final String glob) {
            return glob.substring(0, glob.length() - 1); // remove trailing star
        }

        @Override
        public final boolean matches(final String globPattern, final String name) {
            return name.startsWith(globPattern);
        }
    },
    EXACT {
        @Override
        public final boolean matches(final String globPattern, final String name) {
            return globPattern.equals(name);
        }
    },
    PATTERN {
        @Override
        public final boolean matches(final String globPattern, final String name) {
            int checkIndex = 0;
            for (final String token : Tokens.splitByStar(globPattern)) {
                if (checkIndex == 0 && globPattern.charAt(0) != '*') {
                    // initial match is stricter when pattern doesn't have a leading star
                    if (!name.startsWith(token)) {
                        return false;
                    }
                    checkIndex = token.length();
                } else {
                    // subsequent tokens must appear somewhere after the previous match
                    final int matchIndex = name.indexOf(token, checkIndex);
                    if (matchIndex < 0) {
                        return false;
                    }
                    checkIndex = matchIndex + token.length();
                }
            }
            // pattern matches if we've checked the entire name or there was a trailing star
            return checkIndex == name.length() || globPattern.charAt(globPattern.length() - 1) == '*';
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
    public static final GlobberStrategy selectFor(final String glob) {
        if (null == glob || "*".equals(glob)) {
            return GlobberStrategy.ANYTHING;
        }
        final int firstWildcard = glob.indexOf('*');
        if (firstWildcard < 0) {
            return GlobberStrategy.EXACT;
        }
        final int lastWildcard = glob.lastIndexOf('*');
        if (firstWildcard == lastWildcard) {
            if (firstWildcard == 0) {
                return GlobberStrategy.SUFFIX;
            }
            if (lastWildcard == glob.length() - 1) {
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
    public String compile(final String glob) {
        return glob;
    }

    /**
     * Attempts to match the given compiled glob pattern against a name.
     *
     * @param globPattern The compiled glob pattern
     * @param name The candidate name
     * @return {@code true} if the pattern matches; otherwise {@code false}
     */
    public abstract boolean matches(final String globPattern, final String name);

    /**
     * Attempts to match the given compiled glob pattern against the basename of a path.
     *
     * @param globPattern The compiled glob pattern
     * @param path The candidate path
     * @return {@code true} if the pattern matches; otherwise {@code false}
     */
    public final boolean basenameMatches(final String globPattern, final String path) {
        if (this == ANYTHING || this == SUFFIX) {
            return matches(globPattern, path); // no need to extract basename
        } else {
            return matches(globPattern, path.substring(1 + path.lastIndexOf('/')));
        }
    }
}
