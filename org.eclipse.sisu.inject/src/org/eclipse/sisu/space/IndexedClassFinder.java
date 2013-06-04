/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

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

import org.eclipse.sisu.inject.Logs;

/**
 * {@link ClassFinder} that finds {@link Class} resources listed in the named index.
 */
public class IndexedClassFinder
    implements ClassFinder
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String indexPath;

    private final String indexName;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public IndexedClassFinder( final String name, final boolean globalIndex )
    {
        if ( globalIndex )
        {
            indexPath = null;
            indexName = name;
        }
        else
        {
            final int i = name.lastIndexOf( '/' ) + 1;
            indexPath = name.substring( 0, i );
            indexName = name.substring( i );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Enumeration<URL> findClasses( final ClassSpace space )
    {
        final List<URL> components = new ArrayList<URL>();
        final Set<String> visited = new HashSet<String>();
        final Enumeration<URL> indices;

        if ( null == indexPath )
        {
            indices = space.getResources( indexName );
        }
        else
        {
            indices = space.findEntries( indexPath, indexName, false );
        }

        while ( indices.hasMoreElements() )
        {
            final URL url = indices.nextElement();
            try
            {
                final BufferedReader reader =
                    new BufferedReader( new InputStreamReader( Streams.open( url ), "UTF-8" ) );
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
