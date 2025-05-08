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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.sisu.inject.DeferredClass;
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

    private URL[] bundleClassPath;

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
    public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
    {
        final URL[] classPath = getBundleClassPath();
        final Enumeration<URL> entries = bundle.findEntries( null != path ? path : "/", glob, recurse );
        if ( classPath.length > 0 )
        {
            return new ChainedEnumeration<URL>( entries, new ResourceEnumeration( path, glob, recurse, classPath ) );
        }
        return null != entries ? entries : NO_ENTRIES;
    }

    public Bundle getBundle()
    {
        return bundle;
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
     */
    private synchronized URL[] getBundleClassPath()
    {
        if ( null == bundleClassPath )
        {
            final String path = bundle.getHeaders().get( Constants.BUNDLE_CLASSPATH );
            if ( null == path )
            {
                bundleClassPath = NO_URLS;
            }
            else
            {
                final List<URL> classPath = new ArrayList<URL>();
                final Set<String> visited = new HashSet<String>();

                visited.add( "." );

                for ( final String entry : Tokens.splitByComma( path ) )
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

                bundleClassPath = classPath.isEmpty() ? NO_URLS : classPath.toArray( new URL[classPath.size()] );
            }
        }
        return bundleClassPath;
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
            for ( ; index < enumerations.length; index++ )
            {
                if ( null != enumerations[index] && enumerations[index].hasMoreElements() )
                {
                    return true;
                }
            }
            return false;
        }

        public T nextElement()
        {
            if ( hasMoreElements() )
            {
                return enumerations[index].nextElement();
            }
            throw new NoSuchElementException();
        }
    }
}
