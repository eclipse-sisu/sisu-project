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
    // Utility methods
    // ----------------------------------------------------------------------

    public static String getBasedir()
    {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    public static String getTestPath( final String basedir, final String path )
    {
        throw new UnsupportedOperationException();
    }

    public static String getTestConfiguration( final Class<?> clazz )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final String getTestConfiguration()
    {
        throw new UnsupportedOperationException();
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

    protected final String getConfigurationName( final String name )
    {
        throw new UnsupportedOperationException();
    }

    protected final ClassLoader getClassLoader()
    {
        throw new UnsupportedOperationException();
    }

    protected final Object lookup( final String role )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    protected final Object lookup( final String role, final String hint )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    protected final <T> T lookup( final Class<T> role )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    protected final <T> T lookup( final Class<T> role, final String hint )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    protected final void release( final Object component )
        throws ComponentLifecycleException
    {
        throw new UnsupportedOperationException();
    }
}
