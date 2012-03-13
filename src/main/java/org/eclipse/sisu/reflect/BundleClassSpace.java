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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * {@link ClassSpace} backed by a strongly-referenced {@link Bundle}.
 */
public final class BundleClassSpace
    implements ClassSpace
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final URL[] NO_URLS = {};

    private static final Enumeration<URL> NO_ENTRIES = Collections.enumeration( Collections.<URL> emptySet() );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Bundle bundle;

    private URL[] classPath;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BundleClassSpace( final Bundle bundle )
    {
        this.bundle = bundle;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<?> loadClass( final String name )
    {
        try
        {
            return bundle.loadClass( name );
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
        return bundle.getResource( name );
    }

    @SuppressWarnings( "unchecked" )
    public Enumeration<URL> getResources( final String name )
    {
        try
        {
            final Enumeration<URL> resources = bundle.getResources( name );
            return null != resources ? resources : NO_ENTRIES;
        }
        catch ( final IOException e )
        {
            return NO_ENTRIES;
        }
    }

    @SuppressWarnings( "unchecked" )
    public synchronized Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
    {
        if ( null == classPath )
        {
            classPath = getBundleClassPath( bundle );
        }
        final Enumeration<URL> entries = bundle.findEntries( null != path ? path : "/", glob, recurse );
        if ( classPath.length > 0 )
        {
            return new ChainedEnumeration<URL>( entries, new ResourceEnumeration( path, glob, recurse, classPath ) );
        }
        return null != entries ? entries : NO_ENTRIES;
    }

    @Override
    public int hashCode()
    {
        return bundle.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof BundleClassSpace )
        {
            return bundle.equals( ( (BundleClassSpace) rhs ).bundle );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return bundle.toString();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns the expanded Bundle-ClassPath; we need this to iterate over embedded JARs.
     * 
     * @param bundle The bundle
     * @return URL class path
     */
    private static URL[] getBundleClassPath( final Bundle bundle )
    {
        final String path = (String) bundle.getHeaders().get( Constants.BUNDLE_CLASSPATH );
        if ( null == path )
        {
            return NO_URLS;
        }

        final List<URL> classPath = new ArrayList<URL>();
        final Set<String> visited = new HashSet<String>();

        visited.add( "." );

        for ( final String entry : path.trim().split( "\\s*,\\s*" ) )
        {
            if ( visited.add( entry ) )
            {
                final URL url = bundle.getEntry( entry );
                if ( null != url )
                {
                    classPath.add( url );
                }
            }
        }
        return classPath.isEmpty() ? NO_URLS : classPath.toArray( new URL[classPath.size()] );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Chains a series of {@link Enumeration}s together to look like a single {@link Enumeration}.
     */
    private static final class ChainedEnumeration<T>
        implements Enumeration<T>
    {
        // ----------------------------------------------------------------------
        // Implementation methods
        // ----------------------------------------------------------------------

        private final Enumeration<T>[] enumerations;

        private Enumeration<T> currentEnumeration;

        private T nextElement;

        private int index;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ChainedEnumeration( final Enumeration<T>... enumerations )
        {
            this.enumerations = enumerations;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasMoreElements()
        {
            while ( null == nextElement )
            {
                if ( null != currentEnumeration && currentEnumeration.hasMoreElements() )
                {
                    nextElement = currentEnumeration.nextElement();
                }
                else if ( index < enumerations.length )
                {
                    currentEnumeration = enumerations[index++];
                }
                else
                {
                    return false; // no more elements
                }
            }
            return true;
        }

        public T nextElement()
        {
            if ( hasMoreElements() )
            {
                final T element = nextElement;
                nextElement = null;
                return element;
            }
            throw new NoSuchElementException();
        }
    }
}
