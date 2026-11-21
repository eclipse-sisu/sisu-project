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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utility methods for dealing with streams.
 */
public final class Streams {
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static {
        boolean useCaches;
        try {
            String urlCaches = System.getProperty("sisu.url.caches");
            // Cached jar: URLs keep the JarFile open for the lifetime of the JVM (JDK-8239054),
            // so streams are uncached by default on every platform; set the property to true
            // to restore the old behaviour.
            useCaches = null != urlCaches && !urlCaches.isEmpty() && Boolean.parseBoolean(urlCaches);
        } catch (final RuntimeException e) {
            useCaches = false;
        }
        USE_CACHES = useCaches;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean USE_CACHES;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Streams() {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Opens an input stream to the given URL with caches disabled by default, so that
     * closing the stream closes the underlying JAR file (JDK-8239054). Caches can be
     * enabled by setting the 'sisu.url.caches' system property to {@code true}.
     */
    public static InputStream open(final URL url) throws IOException {
        if (USE_CACHES) {
            return url.openStream();
        }

        final URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        return conn.getInputStream();
    }
}
