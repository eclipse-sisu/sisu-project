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

import java.io.File;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * {@link Iterator} that iterates over named entries beneath a file-system directory.
 */
final class FileEntryIterator
    implements Iterator<String>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String rootPath;

    private final Deque<String> entryNames = new ArrayDeque<String>();

    private final boolean recurse;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates an iterator that iterates over entries beneath the given file URL and sub-path.
     * 
     * @param url The root file URL
     * @param subPath The path below the root URL
     * @param recurse When {@code true} include sub-directories; otherwise don't
     */
    FileEntryIterator( final URL url, final String subPath, final boolean recurse )
    {
        rootPath = normalizePath( toFile( url ).getAbsoluteFile() );
        this.recurse = recurse;
        appendEntries( subPath );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasNext()
    {
        return entryNames.size() > 0;
    }

    public String next() // NOSONAR
    {
        final String name = entryNames.removeFirst();
        if ( recurse && name.endsWith( "/" ) )
        {
            appendEntries( name );
        }
        return name;
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * Converts a {@link URL} into a {@link File} converting slashes and encoded characters where appropriate.
     * 
     * @param url The file URL
     * @return File instance for the given URL
     */
    static File toFile( final URL url )
    {
        final StringBuilder buf = new StringBuilder();

        final String authority = url.getAuthority();
        if ( null != authority && authority.length() > 0 )
        {
            buf.append( File.separatorChar ).append( File.separatorChar ).append( authority );
        }

        final String path = url.getPath();
        int codePoint = 0, expectBytes = 0;
        for ( int i = 0, length = path.length(); i < length; i++ )
        {
            final char c = path.charAt( i );
            if ( '/' == c )
            {
                buf.append( File.separatorChar );
            }
            else if ( '%' == c && i < length - 2 )
            {
                final int hi = Character.digit( path.charAt( i + 1 ), 16 );
                final int lo = Character.digit( path.charAt( i + 2 ), 16 );
                if ( hi >= 0 && lo >= 0 )
                {
                    if ( hi < 8 )
                    {
                        buf.append( (char) ( hi << 4 | lo ) );
                    }
                    else if ( hi >= 12 )
                    {
                        // prepare multi-byte UTF-8 sequence
                        expectBytes = 12 == hi ? 1 : hi - 12;
                        codePoint = ( 13 == hi ? 0x10 + lo : lo ) << 6 * expectBytes;
                    }
                    else if ( expectBytes > 0 )
                    {
                        // update multi-byte UTF-8 sequence
                        codePoint |= ( ( 0x03 & hi ) << 4 | lo ) << 6 * --expectBytes;
                        if ( expectBytes == 0 )
                        {
                            buf.appendCodePoint( codePoint );
                        }
                    }
                    i += 2;
                }
                else
                {
                    buf.append( '%' );
                }
            }
            else
            {
                buf.append( c );
            }
        }
        return new File( buf.toString() );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Appends entries from the given sub-path to the cached list of named entries.
     * 
     * @param subPath The sub path
     */
    private void appendEntries( final String subPath )
    {
        final File[] listing = new File( rootPath + subPath ).listFiles();
        if ( null != listing )
        {
            for ( final File f : listing )
            {
                entryNames.add( normalizePath( f ).substring( rootPath.length() ) );
            }
        }
    }

    /**
     * Returns the normalized URI path of the given file.
     * 
     * @param file The file to normalize
     * @return Normalized URI path
     */
    private static String normalizePath( final File file )
    {
        return file.toURI().getPath();
    }
}
