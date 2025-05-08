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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sisu.inject.Logs;

/**
 * Command-line utility that generates a qualified class index for a space-separated list of JARs.
 * <p>
 * The index consists of qualified class names listed in {@code META-INF/sisu/javax.inject.Named}.
 * 
 * @see <a href="http://eclipse.org/sisu/docs/api/org.eclipse.sisu.mojos/">sisu-maven-plugin</a>
 */
public class SisuIndex
    extends AbstractSisuIndex
    implements SpaceVisitor, ClassVisitor
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final QualifierCache qualifierCache = new QualifierCache( true );

    private final File targetDirectory;

    private ClassSpace space;

    private String clazzName;

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

    public final void index( final ClassSpace _space )
    {
        try
        {
            new SpaceScanner( _space, true ).accept( this );
        }
        finally
        {
            flushIndex();
        }
    }

    public final void enterSpace( final ClassSpace _space )
    {
        space = _space;
    }

    public final ClassVisitor visitClass( final URL url )
    {
        return this;
    }

    public final void enterClass( final int modifiers, final String name, final String _extends,
                                  final String[] _implements )
    {
        if ( ( modifiers & NON_INSTANTIABLE ) == 0 )
        {
            clazzName = name; // concrete type
        }
    }

    public final AnnotationVisitor visitAnnotation( final String desc )
    {
        if ( null != clazzName && qualifierCache.qualify( space, desc ) )
        {
            addClassToIndex( NAMED, clazzName.replace( '/', '.' ) );
        }
        return null;
    }

    public final void leaveClass()
    {
        clazzName = null;
    }

    public final void leaveSpace()
    {
        space = null;
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
        return new InputStreamReader( new FileInputStream( new File( targetDirectory, path ) ), "UTF-8" );
    }

    @Override
    protected Writer getWriter( final String path )
        throws IOException
    {
        final File index = new File( targetDirectory, path );
        final File parent = index.getParentFile();
        if ( parent.isDirectory() || parent.mkdirs() )
        {
            return new OutputStreamWriter( new FileOutputStream( index ), "UTF-8" );
        }
        throw new IOException( "Error creating: " + parent );
    }
}
