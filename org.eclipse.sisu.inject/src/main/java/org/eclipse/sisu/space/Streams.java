/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
import java.util.Locale;

/**
 * Utility methods for dealing with streams.
 */
public final class Streams
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean useCaches;
        try
        {
            String urlCaches = System.getProperty( "sisu.url.caches" );
            if ( null != urlCaches && !urlCaches.isEmpty() )
            {
                useCaches = Boolean.parseBoolean( urlCaches );
            }
            else
            {
                String osName = System.getProperty( "os.name" ).toLowerCase( Locale.US );
                useCaches = !osName.contains( "windows" );
            }
        }
        catch ( final RuntimeException e )
        {
            useCaches = true;
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

    private Streams()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Opens an input stream to the given URL; disables JAR caching on Windows
     * or when the 'sisu.url.caches' system property is set to {@code false}.
     */
    public static InputStream open( final URL url )
        throws IOException
    {
        if ( USE_CACHES )
        {
            return url.openStream();
        }

        final URLConnection conn = url.openConnection();
        conn.setUseCaches( false );
        return conn.getInputStream();
    }
}
