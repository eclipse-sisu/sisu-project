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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.reflect.Logs;
import org.eclipse.sisu.reflect.URLClassSpace;
import org.eclipse.sisu.scanners.ClassSpaceScanner;
import org.eclipse.sisu.scanners.QualifiedTypeListener;
import org.eclipse.sisu.scanners.QualifiedTypeVisitor;

/**
 * Command-line utility that generates a qualified class index for a space-separated list of JARs.
 */
public final class SisuIndex
    extends AbstractSisuIndex
    implements QualifiedTypeListener
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    public static final String NAMED = Named.class.getName();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final File targetDirectory;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SisuIndex( final File targetDirectory )
    {
        this.targetDirectory = targetDirectory;
    }

    // ----------------------------------------------------------------------
    // Public entry points
    // ----------------------------------------------------------------------

    public static void main( final String[] args )
    {
        final List<URL> indexPath = new ArrayList<URL>( args.length );
        for ( final String path : args )
        {
            try
            {
                indexPath.add( new File( path ).toURI().toURL() );
            }
            catch ( final MalformedURLException e )
            {
                Logs.warn( "Bad classpath element: {}", path, e );
            }
        }

        final ClassLoader parent = SisuIndex.class.getClassLoader();
        final URL[] urls = indexPath.toArray( new URL[indexPath.size()] );
        final ClassLoader loader = urls.length > 0 ? URLClassLoader.newInstance( urls, parent ) : parent;

        new SisuIndex( new File( "." ) ).index( new URLClassSpace( loader ) );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void index( final ClassSpace space )
    {
        try
        {
            new ClassSpaceScanner( space ).accept( new QualifiedTypeVisitor( this ) );
        }
        finally
        {
            flushIndex();
        }
    }

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        addClassToIndex( SisuIndex.NAMED, qualifiedType.getName() );
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected void info( final String message )
    {
        System.out.println( "[INFO] " + message );
    }

    @Override
    protected void warn( final String message )
    {
        System.out.println( "[WARN] " + message );
    }

    @Override
    protected Reader getReader( final String path )
        throws IOException
    {
        return new FileReader( new File( targetDirectory, path ) );
    }

    @Override
    protected Writer getWriter( final String path )
        throws IOException
    {
        final File index = new File( targetDirectory, path );
        final File parent = index.getParentFile();
        if ( parent.isDirectory() || parent.mkdirs() )
        {
            return new FileWriter( index );
        }
        throw new IOException( "Error creating: " + parent );
    }
}
