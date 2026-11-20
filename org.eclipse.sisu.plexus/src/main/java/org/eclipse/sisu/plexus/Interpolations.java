/*
 * Copyright (c) 2026 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sisu.plexus;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import org.codehaus.plexus.util.xml.XmlStreamReader;

/**
 * Small internal helpers that replace the plexus-utils interpolation and stream utilities used by the adapter.
 */
final class Interpolations {

    private Interpolations() {
        // static utility class
    }

    /**
     * Expands {@code ${key}} tokens using the supplied variables. A missing key leaves the token untouched and a
     * present key with an empty value removes the token, matching the historical plexus-utils behavior.
     *
     * @param text text to interpolate; may be {@code null}
     * @param variables variable values; may be {@code null}
     * @return the interpolated text
     */
    static String interpolate(final String text, final Map<?, ?> variables) {
        if (text == null || variables == null || !text.contains("${")) {
            return text;
        }
        final StringBuilder result = new StringBuilder(text.length());
        int position = 0;
        while (position < text.length()) {
            final int start = text.indexOf("${", position);
            if (start < 0) {
                result.append(text, position, text.length());
                break;
            }
            final int end = text.indexOf('}', start + 2);
            if (end < 0) {
                result.append(text, position, text.length());
                break;
            }
            result.append(text, position, start);
            final Object value = variables.get(text.substring(start + 2, end));
            if (value != null) {
                result.append(value);
            } else {
                result.append(text, start, end + 1);
            }
            position = end + 1;
        }
        return result.toString();
    }

    /**
     * Reads a whole character stream into a string.
     *
     * @param in stream to read
     * @return the stream contents
     * @throws IOException if the stream cannot be read
     */
    static String toString(final Reader in) throws IOException {
        final StringBuilder result = new StringBuilder();
        final char[] buffer = new char[4096];
        int count;
        while ((count = in.read(buffer)) != -1) {
            result.append(buffer, 0, count);
        }
        return result.toString();
    }

    /**
     * Wraps an XML stream with encoding detection.
     *
     * @param in the XML input
     * @return a reader over the stream
     * @throws IOException if the reader cannot be created
     */
    static Reader xmlReader(final InputStream in) throws IOException {
        return new XmlStreamReader(in);
    }

    /**
     * Interpolates the contents of a reader into a new reader.
     *
     * @param in the source reader
     * @param variables variable values; may be {@code null}
     * @return a reader over the interpolated contents
     * @throws IOException if the source cannot be read
     */
    static Reader interpolatingReader(final Reader in, final Map<?, ?> variables) throws IOException {
        if (variables == null) {
            return in;
        }
        return new StringReader(interpolate(toString(in), variables));
    }

    /**
     * Null-safe stream close used by the XML scanners.
     *
     * @param in stream to close; may be {@code null}
     */
    static void close(final InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (final IOException e) {
                // ignore, as the original utility did
            }
        }
    }
}
