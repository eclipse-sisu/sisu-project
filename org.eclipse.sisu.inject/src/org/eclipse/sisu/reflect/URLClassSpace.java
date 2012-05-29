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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * {@link ClassSpace} backed by a strongly-referenced {@link ClassLoader} and a {@link URL} class path.
 */
public final class URLClassSpace
    implements ClassSpace
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String MANIFEST_ENTRY = "META-INF/MANIFEST.MF";

    private static final URL[] NO_URLS = {};

    private static final Enumeration<URL> NO_ENTRIES = Collections.enumeration( Collections.<URL> emptySet() );

    private static final String[] EMPTY_CLASSPATH = {};

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassLoader loader;

    private URL[] classPath;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link ClassSpace} backed by a {@link ClassLoader} and its default class path.
     * <p>
     * For {@link URLClassLoader}s this is their expanded Class-Path; otherwise it is empty.
     * 
     * @param loader The class loader to use when getting/finding resources
     */
    public URLClassSpace( final ClassLoader loader )
    {
        this.loader = loader;
        // compute class path on demand
    }

    /**
     * Creates a {@link ClassSpace} backed by a {@link ClassLoader} with a restricted class path.
     * 
     * @param loader The class loader to use when getting resources
     * @param path The class path to use when finding resources
     * @see #getResources(String)
     * @see #findEntries(String, String, boolean)
     */
    public URLClassSpace( final ClassLoader loader, final URL[] path )
    {
        this.loader = loader;
        if ( null != path && path.length > 0 )
        {
            classPath = expandClassPath( path );
        }
        else
        {
            classPath = NO_URLS;
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<?> loadClass( final String name )
    {
        try
        {
            return loader.loadClass( name );
        }
        catch ( final Exception e )
        {
            throw new TypeNotPresentException( name, e );
        }
        catch ( final LinkageError e )
        {
            throw new TypeNotPresentException( name, e );
        }
    }

    public DeferredClass<?> deferLoadClass( final String name )
    {
        return new NamedClass<Object>( this, name );
    }

    public URL getResource( final String name )
    {
        return loader.getResource( name );
    }

    public Enumeration<URL> getResources( final String name )
    {
        try
        {
            final Enumeration<URL> resources = loader.getResources( name );
            return null != resources ? resources : NO_ENTRIES;
        }
        catch ( final IOException e )
        {
            return NO_ENTRIES;
        }
    }

    public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
    {
        return new ResourceEnumeration( path, glob, recurse, getClassPath() );
    }

    public URL[] getURLs()
    {
        return getClassPath().clone();
    }

    @Override
    public int hashCode()
    {
        return loader.hashCode(); // the loader is the primary key
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof URLClassSpace )
        {
            return loader.equals( ( (URLClassSpace) rhs ).loader );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return loader.toString(); // TODO: include classpath details?
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns the associated {@link URL} class path; this can either be explicit or implicit.
     */
    private synchronized URL[] getClassPath()
    {
        if ( null == classPath )
        {
            for ( ClassLoader l = loader; l != null; l = l.getParent() )
            {
                if ( l instanceof URLClassLoader )
                {
                    // pick first class loader with non-empty class path
                    final URL[] path = ( (URLClassLoader) l ).getURLs();
                    if ( null != path && path.length > 0 )
                    {
                        classPath = expandClassPath( path );
                        break;
                    }
                }
            }
            if ( null == classPath )
            {
                classPath = NO_URLS; // no detectable Class-Path
            }
        }
        return classPath;
    }

    /**
     * Expands the given {@link URL} class path to include Class-Path entries from local manifests.
     * 
     * @param classPath The URL class path
     * @return Expanded URL class path
     */
    private static URL[] expandClassPath( final URL[] classPath )
    {
        final List<URL> searchPath = new ArrayList<URL>();
        Collections.addAll( searchPath, classPath );

        final List<URL> expandedPath = new ArrayList<URL>();
        final Set<String> visited = new HashSet<String>();

        // search path may grow, so use index not iterator
        for ( int i = 0; i < searchPath.size(); i++ )
        {
            final URL url = normalizeEntry( searchPath.get( i ) );
            if ( null == url || !visited.add( url.toString() ) )
            {
                continue; // already processed
            }
            expandedPath.add( url );
            final String[] classPathEntries;
            try
            {
                classPathEntries = getClassPathEntries( url );
            }
            catch ( final IOException e )
            {
                continue; // missing manifest
            }
            for ( final String entry : classPathEntries )
            {
                try
                {
                    searchPath.add( new URL( url, entry ) );
                }
                catch ( final MalformedURLException e ) // NOPMD
                {
                    // invalid Class-Path entry
                }
            }
        }

        return expandedPath.toArray( new URL[expandedPath.size()] );
    }

    /**
     * Normalizes the given class path entry by removing any extraneous "jar:"..."!/" padding.
     * 
     * @param path The URL to normalize
     * @return Normalized class path entry
     */
    private static URL normalizeEntry( final URL url )
    {
        if ( null != url && "jar".equals( url.getProtocol() ) )
        {
            final String path = url.getPath();
            if ( path.endsWith( "!/" ) )
            {
                try
                {
                    return new URL( path.substring( 0, path.length() - 2 ) );
                }
                catch ( final MalformedURLException e )
                {
                    // this shouldn't happen, hence illegal state
                    throw new IllegalStateException( e.toString() );
                }
            }
        }
        return url;
    }

    /**
     * Looks for Class-Path entries in the given jar or directory; returns empty array if none are found.
     * 
     * @param url The jar or directory to inspect
     * @return Array of Class-Path entries
     */
    private static String[] getClassPathEntries( final URL url )
        throws IOException
    {
        final Manifest manifest;
        if ( url.getPath().endsWith( "/" ) )
        {
            final InputStream in = Streams.open( new URL( url, MANIFEST_ENTRY ) );
            try
            {
                manifest = new Manifest( in );
            }
            finally
            {
                in.close();
            }
        }
        else if ( "file".equals( url.getProtocol() ) )
        {
            final JarFile jf = new JarFile( FileEntryIterator.toFile( url ) );
            try
            {
                manifest = jf.getManifest();
            }
            finally
            {
                jf.close();
            }
        }
        else
        {
            final JarInputStream jin = new JarInputStream( Streams.open( url ) );
            try
            {
                manifest = jin.getManifest();
            }
            finally
            {
                jin.close();
            }
        }
        if ( null != manifest )
        {
            final String classPath = manifest.getMainAttributes().getValue( "Class-Path" );
            if ( null != classPath )
            {
                return classPath.split( " " );
            }
        }
        return EMPTY_CLASSPATH;
    }
}
