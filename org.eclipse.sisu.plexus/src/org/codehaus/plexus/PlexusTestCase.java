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
        return getTestFile( getBasedir(), path );
    }

    public static File getTestFile( final String basedir, final String path )
    {
        File root = new File( basedir );
        if ( !root.isAbsolute() )
        {
            root = new File( getBasedir(), basedir );
        }
        return new File( root, path );
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
    // Implementation fields
    // ----------------------------------------------------------------------

    private volatile PlexusContainer container;

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
        // place-holder for tests to customize
    }

    protected void customizeContext( @SuppressWarnings( "unused" ) final Context context )
    {
        // place-holder for tests to customize
    }

    protected String getCustomConfigurationName()
    {
        return null; // place-holder for tests to customize
    }

    protected void customizeContainerConfiguration( @SuppressWarnings( "unused" ) final ContainerConfiguration configuration )
    {
        // place-holder for tests to customize
    }

    protected synchronized void setupContainer()
    {
        if ( null == container )
        {
            // FIXME!
        }
    }

    protected synchronized void teardownContainer()
    {
        if ( null != container )
        {
            container.dispose();
            container = null;
        }
    }

    protected PlexusContainer getContainer()
    {
        if ( null == container )
        {
            setupContainer();
        }
        return container;
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        if ( null != container )
        {
            teardownContainer();
        }
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
