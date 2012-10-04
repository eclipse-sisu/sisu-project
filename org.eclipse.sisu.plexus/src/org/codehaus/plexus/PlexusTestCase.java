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
package org.codehaus.plexus;

import java.io.File;

import junit.framework.TestCase;

import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;

public abstract class PlexusTestCase
    extends TestCase
{
    // ----------------------------------------------------------------------
    // Initialization-on-demand
    // ----------------------------------------------------------------------

    private static final class Lazy
    {
        static
        {
            final String path = System.getProperty( "basedir" );
            BASEDIR = null != path ? path : new File( "" ).getAbsolutePath();
        }

        static final String BASEDIR;
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static String getBasedir()
    {
        return Lazy.BASEDIR;
    }

    public static File getTestFile( final String path )
    {
        throw new UnsupportedOperationException();
    }

    public static File getTestFile( final String basedir, final String path )
    {
        throw new UnsupportedOperationException();
    }

    public static String getTestPath( final String path )
    {
        return getTestFile( path ).getAbsolutePath();
    }

    public static String getTestPath( final String basedir, final String path )
    {
        return getTestFile( basedir, path ).getAbsolutePath();
    }

    public static String getTestConfiguration( final Class<?> clazz )
    {
        // always use outermost class name
        final String name = clazz.getName();
        final int i = name.indexOf( '$' );

        return ( i < 0 ? name : name.substring( 0, i ) ).replace( '.', '/' ) + ".xml";
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final String getTestConfiguration()
    {
        return getTestConfiguration( getClass() );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    @Override
    protected void setUp()
        throws Exception
    {
        throw new UnsupportedOperationException();
    }

    protected void customizeContext( final Context context )
    {
        throw new UnsupportedOperationException();
    }

    protected String getCustomConfigurationName()
    {
        throw new UnsupportedOperationException();
    }

    protected void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        throw new UnsupportedOperationException();
    }

    protected void setupContainer()
    {
        throw new UnsupportedOperationException();
    }

    protected PlexusContainer getContainer()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    protected final String getConfigurationName( @SuppressWarnings( "unused" ) final String name )
    {
        return getTestConfiguration();
    }

    protected final ClassLoader getClassLoader()
    {
        return getClass().getClassLoader();
    }

    protected final Object lookup( final String role )
        throws ComponentLookupException
    {
        return getContainer().lookup( role );
    }

    protected final Object lookup( final String role, final String hint )
        throws ComponentLookupException
    {
        return getContainer().lookup( role, hint );
    }

    protected final <T> T lookup( final Class<T> role )
        throws ComponentLookupException
    {
        return getContainer().lookup( role );
    }

    protected final <T> T lookup( final Class<T> role, final String hint )
        throws ComponentLookupException
    {
        return getContainer().lookup( role, hint );
    }

    protected final void release( final Object component )
        throws ComponentLifecycleException
    {
        getContainer().release( component );
    }
}
