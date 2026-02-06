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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.eclipse.sisu.inject.Logs;

/**
 * {@link ClassFinder} that finds {@link Class} resources listed in the named index.
 */
public final class IndexedClassFinder implements ClassFinder {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String localPath;

    private final String indexName;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public IndexedClassFinder(final String name, final boolean global) {
        if (global) {
            localPath = null;
            indexName = name;
        } else {
            final int i = name.lastIndexOf('/') + 1;
            localPath = name.substring(0, i);
            indexName = name.substring(i);
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterable<String> indexedNames(final ClassSpace space) {
        final Enumeration<URL> indices;

        if (null == localPath) {
            indices = space.getResources(indexName);
        } else {
            indices = space.findEntries(localPath, indexName, false);
        }

        final Set<String> names = new LinkedHashSet<>();
        while (indices.hasMoreElements()) {
            final URL url = indices.nextElement();
            try {
                try (BufferedReader reader =
                        new BufferedReader(new InputStreamReader(Streams.open(url), StandardCharsets.UTF_8))) {
                    // each index contains a list of class names, one per line with optional comment
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        final int i = line.indexOf('#');
                        if (i == 0) {
                            continue; // entire line is a comment, ignore it
                        }
                        final String name = (i < 0 ? line : line.substring(0, i)).trim();
                        if (!name.isEmpty()) {
                            names.add(name);
                        }
                    }
                }
            } catch (final IOException e) {
                Logs.warn("Problem reading: {}", url, e);
            }
        }
        return names;
    }

    @Override
    public Enumeration<URL> findClasses(final ClassSpace space) {
        final Iterator<String> itr = indexedNames(space).iterator();

        return new Enumeration<URL>() {
            private URL nextURL;

            @Override
            public boolean hasMoreElements() {
                while (null == nextURL && itr.hasNext()) {
                    nextURL = space.getResource(itr.next().replace('.', '/') + ".class");
                }
                return null != nextURL;
            }

            @Override
            public URL nextElement() {
                if (hasMoreElements()) {
                    final URL tempURL = nextURL;
                    nextURL = null;
                    return tempURL;
                }
                throw new NoSuchElementException();
            }
        };
    }
}
