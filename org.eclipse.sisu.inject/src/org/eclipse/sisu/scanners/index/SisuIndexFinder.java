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
package org.eclipse.sisu.scanners.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.reflect.Logs;
import org.eclipse.sisu.reflect.Streams;
import org.eclipse.sisu.scanners.ClassFinder;

/**
 * {@link ClassFinder} that uses the qualified class index to select implementations to scan.
 */
public final class SisuIndexFinder
    implements ClassFinder
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final boolean globalIndex;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SisuIndexFinder( final boolean globalIndex )
    {
        this.globalIndex = globalIndex;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Enumeration<URL> findClasses( final ClassSpace space )
    {
        final List<URL> components = new ArrayList<URL>();
        final Set<String> visited = new HashSet<String>();
        final Enumeration<URL> indices;

        if ( globalIndex )
        {
            indices = space.getResources( "META-INF/sisu/" + SisuIndex.NAMED );
        }
        else
        {
            indices = space.findEntries( "META-INF/sisu/", SisuIndex.NAMED, false );
        }

        while ( indices.hasMoreElements() )
        {
            final URL url = indices.nextElement();
            try
            {
                final BufferedReader reader = new BufferedReader( new InputStreamReader( Streams.open( url ) ) );
                try
                {
                    // each index file contains a list of classes with that qualifier, one per line
                    for ( String line = reader.readLine(); line != null; line = reader.readLine() )
                    {
                        if ( visited.add( line ) )
                        {
                            final URL clazz = space.getResource( line.replace( '.', '/' ) + ".class" );
                            if ( null != clazz )
                            {
                                components.add( clazz );
                            }
                        }
                    }
                }
                finally
                {
                    reader.close();
                }
            }
            catch ( final IOException e )
            {
                Logs.warn( "Problem reading: {}", url, e );
            }
        }
        return Collections.enumeration( components );
    }
}
