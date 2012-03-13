/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.reflect;

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
final class ZipEntryIterator
    implements Iterator<String>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String[] entryNames;

    private int index;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ZipEntryIterator( final URL url )
    {
        try
        {
            if ( "file".equals( url.getProtocol() ) )
            {
                entryNames = getEntryNames( new ZipFile( FileEntryIterator.toFile( url ) ) );
            }
            else
            {
                entryNames = getEntryNames( new ZipInputStream( Streams.open( url ) ) );
            }
        }
        catch ( final IOException e )
        {
            entryNames = new String[0];
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasNext()
    {
        return index < entryNames.length;
    }

    public String next()
    {
        return entryNames[index++];
    }

    public void remove()
    {
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
    private static String[] getEntryNames( final ZipFile zipFile )
        throws IOException
    {
        try
        {
            final String names[] = new String[zipFile.size()];
            final Enumeration<? extends ZipEntry> e = zipFile.entries();
            for ( int i = 0; i < names.length; i++ )
            {
                names[i] = e.nextElement().getName();
            }
            return names;
        }
        finally
        {
            zipFile.close();
        }
    }

    /**
     * Returns a string array listing the entries in the given zip stream.
     * 
     * @param zipStream The zip stream
     * @return Array of entry names
     */
    private static String[] getEntryNames( final ZipInputStream zipStream )
        throws IOException
    {
        try
        {
            final List<String> names = new ArrayList<String>( 64 );
            for ( ZipEntry e = zipStream.getNextEntry(); e != null; e = zipStream.getNextEntry() )
            {
                names.add( e.getName() );
            }
            return names.toArray( new String[names.size()] );
        }
        finally
        {
            zipStream.close();
        }
    }
}
