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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * {@link Iterator} that iterates over named entries inside JAR or ZIP resources.
 */
final class ZipEntryIterator implements Iterator<String> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String[] entryNames;

    private int index;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ZipEntryIterator(final URL url) {
        try {
            if ("file".equals(url.getProtocol())) {
                entryNames = getEntryNames(new ZipFile(FileEntryIterator.toFile(url)));
            } else {
                entryNames = getEntryNames(new ZipInputStream(Streams.open(url)));
            }
        } catch (final IOException e) {
            entryNames = new String[0];
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public boolean hasNext() {
        return index < entryNames.length;
    }

    @Override
    public String next() // NOSONAR
            {
        return entryNames[index++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns a string array listing the entries in the given zip file.
     *
     * @param zipFile The zip file
     * @return Array of entry names
     */
    private static String[] getEntryNames(final ZipFile zipFile) throws IOException {
        try {
            final String names[] = new String[zipFile.size()];
            final Enumeration<? extends ZipEntry> e = zipFile.entries(); // NOSONAR
            for (int i = 0; i < names.length; i++) {
                names[i] = e.nextElement().getName();
            }
            return names;
        } finally {
            zipFile.close();
        }
    }

    /**
     * Returns a string array listing the entries in the given zip stream.
     *
     * @param zipStream The zip stream
     * @return Array of entry names
     */
    private static String[] getEntryNames(final ZipInputStream zipStream) throws IOException {
        try {
            final List<String> names = new ArrayList<>(64);
            for (ZipEntry e = zipStream.getNextEntry(); e != null; e = zipStream.getNextEntry()) // NOSONAR
            {
                names.add(e.getName());
            }
            return names.toArray(new String[names.size()]);
        } finally {
            zipStream.close();
        }
    }
}
