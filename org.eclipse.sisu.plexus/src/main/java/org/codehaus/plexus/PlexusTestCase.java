/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;

import junit.framework.TestCase;

public abstract class PlexusTestCase
    extends TestCase
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String PLEXUS_HOME = "plexus.home";

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

    private volatile PlexusContainer container; // NOSONAR

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

    @Override
    protected void setUp()
        throws Exception
    {
        // place-holder for tests to customize
    }

    protected PlexusContainer getContainer()
    {
        if ( null == container )
        {
            setupContainer();
        }
        return container;
    }

    protected synchronized void setupContainer()
    {
        if ( null == container )
        {
            try
            {
                container = new DefaultPlexusContainer( config() );
            }
            catch ( final PlexusContainerException e )
            {
                throw new IllegalArgumentException( e );
            }
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

    protected final InputStream getResourceAsStream( final String name )
    {
        return getClass().getResourceAsStream( name );
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

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private final ContainerConfiguration config()
    {
        final ContainerConfiguration config = new DefaultContainerConfiguration();

        // Apply current test context

        config.setName( "test" ).setContext( context() );

        // Find per-test components XML

        String path = getCustomConfigurationName();
        if ( null == path )
        {
            path = getConfigurationName( null );
        }

        config.setContainerConfiguration( path );

        // Per-test config customization

        customizeContainerConfiguration( config );

        return config;
    }

    private final Map<Object, Object> context()
    {
        final Context context = new DefaultContext();
        context.put( "basedir", getBasedir() );

        // Per-test context customization

        customizeContext( context );

        // Provide 'plexus.home' fall-back

        if ( !context.contains( PLEXUS_HOME ) )
        {
            context.put( PLEXUS_HOME, plexusHome() );
        }

        return context.getContextData();
    }

    private static String plexusHome()
    {
        final File home = getTestFile( "target/plexus-home" );
        if ( !home.isDirectory() )
        {
            home.mkdirs();
        }
        return home.getAbsolutePath();
    }
}
