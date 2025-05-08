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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * Skeleton class that generates a qualified class index.
 */
abstract class AbstractSisuIndex
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String INDEX_FOLDER = "META-INF/sisu/";

    static final String QUALIFIER = "javax.inject.Qualifier";

    static final String NAMED = "javax.inject.Named";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<Object, Set<String>> index = new LinkedHashMap<Object, Set<String>>();

    // ----------------------------------------------------------------------
    // Common methods
    // ----------------------------------------------------------------------

    /**
     * Adds a new annotated class entry to the index.
     * 
     * @param anno The annotation name
     * @param clazz The class name
     */
    protected final synchronized void addClassToIndex( final Object anno, final Object clazz )
    {
        Set<String> table = index.get( anno );
        if ( null == table )
        {
            table = readTable( anno );
            index.put( anno, table );
        }
        table.add( String.valueOf( clazz ) );
    }

    /**
     * Writes the current index as a series of tables.
     */
    protected final synchronized void flushIndex()
    {
        for ( final Entry<Object, Set<String>> entry : index.entrySet() )
        {
            writeTable( entry.getKey(), entry.getValue() );
        }
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Reports an informational message.
     * 
     * @param message The message
     */
    protected abstract void info( final String message );

    /**
     * Reports a warning message.
     * 
     * @param message The message
     */
    protected abstract void warn( final String message );

    /**
     * Creates a new reader for the given input path.
     * 
     * @param path The input path
     * @return The relevant reader
     */
    protected abstract Reader getReader( final String path )
        throws IOException;

    /**
     * Creates a new writer for the given output path.
     * 
     * @param path The output path
     * @return The relevant writer
     */
    protected abstract Writer getWriter( final String path )
        throws IOException;

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Reads the given index table from disk to memory.
     * 
     * @param name The table name
     * @return Table elements
     */
    private Set<String> readTable( final Object name )
    {
        final Set<String> table = new TreeSet<String>();
        try
        {
            final BufferedReader reader = new BufferedReader( getReader( INDEX_FOLDER + name ) );
            try
            {
                for ( String line = reader.readLine(); line != null; line = reader.readLine() )
                {
                    table.add( line );
                }
            }
            finally
            {
                reader.close();
            }
        }
        catch ( final IOException e ) // NOPMD
        {
            // ignore missing index
        }
        return table;
    }

    /**
     * Writes the given index table from memory to disk.
     * 
     * @param name The table name
     * @param table The elements
     */
    private void writeTable( final Object name, final Set<String> table )
    {
        try
        {
            final BufferedWriter writer = new BufferedWriter( getWriter( INDEX_FOLDER + name ) );
            try
            {
                for ( final String line : table )
                {
                    writer.write( line );
                    writer.newLine();
                }
            }
            finally
            {
                writer.close();
            }
        }
        catch ( final IOException e )
        {
            warn( e.toString() );
        }
    }
}
