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
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;

public class BundleClassSpaceTest
    extends TestCase
{
    private static final URL EMPTY_BUNDLE = ZipEntryIteratorTest.class.getResource( "empty.jar" );

    private static final URL SIMPLE_BUNDLE = ZipEntryIteratorTest.class.getResource( "simple_bundle.jar" );

    private static final URL LOGGING_BUNDLE = ZipEntryIteratorTest.class.getResource( "logging_bundle.jar" );

    private Framework framework;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        final Map<String, String> configuration = new HashMap<String, String>();
        configuration.put( Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT );
        configuration.put( Constants.FRAMEWORK_STORAGE, "target/bundlecache" );

        final FrameworkFactory frameworkFactory = new FrameworkFactory();
        framework = frameworkFactory.newFramework( configuration );
        framework.start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        framework.stop();
        framework.waitForStop( 0 );

        super.tearDown();
    }

    public void testHashCodeAndEquals()
        throws Exception
    {
        final Bundle testBundle = framework.getBundleContext().installBundle( LOGGING_BUNDLE.toString() );
        final ClassSpace space = new BundleClassSpace( testBundle );

        assertEquals( space, space );

        assertEquals( space, new BundleClassSpace( testBundle ) );

        assertFalse( space.equals( new ClassSpace()
        {
            public Class<?> loadClass( final String name )
            {
                return space.loadClass( name );
            }

            public DeferredClass<?> deferLoadClass( final String name )
            {
                return space.deferLoadClass( name );
            }

            public Enumeration<URL> getResources( final String name )
            {
                return space.getResources( name );
            }

            public URL getResource( final String name )
            {
                return space.getResource( name );
            }

            public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
            {
                return space.findEntries( path, glob, recurse );
            }
        } ) );

        assertEquals( testBundle.hashCode(), space.hashCode() );
        assertEquals( testBundle.toString(), space.toString() );
    }

    public void testClassSpaceResources()
        throws Exception
    {
        final Bundle testBundle1 = framework.getBundleContext().installBundle( LOGGING_BUNDLE.toString() );
        final ClassSpace space1 = new BundleClassSpace( testBundle1 );

        final Bundle testBundle2 = framework.getBundleContext().installBundle( SIMPLE_BUNDLE.toString() );
        final ClassSpace space2 = new BundleClassSpace( testBundle2 );

        final Bundle testBundle3 = framework.getBundleContext().installBundle( EMPTY_BUNDLE.toString() );
        final ClassSpace space3 = new BundleClassSpace( testBundle3 );

        Enumeration<URL> e;

        // logging bundle class-path has repeated entries...
        e = space1.getResources( "META-INF/MANIFEST.MF" );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/META-INF/MANIFEST.MF" ) );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/META-INF/MANIFEST.MF" ) );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/META-INF/MANIFEST.MF" ) );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/META-INF/MANIFEST.MF" ) );
        assertFalse( e.hasMoreElements() );

        // ...which we try to collapse when using find
        e = space1.findEntries( "META-INF", "*.MF", false );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/META-INF/MANIFEST.MF" ) );
        assertTrue( e.nextElement().toString().matches( "jar:bundle://.*!/META-INF/MANIFEST.MF" ) );
        assertFalse( e.hasMoreElements() );

        try
        {
            e.nextElement();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException t )
        {
        }

        e = space2.findEntries( "a/b", "*", true );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/a/b/2" ) );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/a/b/c/" ) );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/a/b/c/3" ) );
        assertFalse( e.hasMoreElements() );

        e = space3.findEntries( "", "*", true );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/META-INF/" ) );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/META-INF/MANIFEST.MF" ) );
        assertFalse( e.hasMoreElements() );

        e = space1.findEntries( null, "missing", true );
        assertFalse( e.hasMoreElements() );
        e = space2.findEntries( null, "missing", true );
        assertFalse( e.hasMoreElements() );
        e = space3.findEntries( null, "missing", true );
        assertFalse( e.hasMoreElements() );

        final URL manifestURL = space1.getResource( "META-INF/MANIFEST.MF" );
        assertNotNull( manifestURL );
        new Manifest( manifestURL.openStream() );
    }

    public void testDeferredClass()
        throws Exception
    {
        final Bundle testBundle = framework.getBundleContext().installBundle( LOGGING_BUNDLE.toString() );
        final ClassSpace space = new BundleClassSpace( testBundle );

        final String clazzName = "org.apache.commons.logging.Log";
        final DeferredClass<?> clazz = space.deferLoadClass( clazzName );

        assertEquals( clazzName, clazz.getName() );
        assertSame( space.loadClass( clazzName ), clazz.load() );

        final DeferredClass<?> missingClazz = space.deferLoadClass( "missing.class" );
        try
        {
            missingClazz.load();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
        }
    }

    public void testBrokenResources()
    {
        final ClassSpace space = new BundleClassSpace( new Bundle()
        {
            public Enumeration<?> getResources( final String name )
                throws IOException
            {
                throw new IOException(); // the rest of the methods aren't used...
            }

            public void update( final InputStream input )
                throws BundleException
            {
            }

            public void update()
                throws BundleException
            {
            }

            public void uninstall()
                throws BundleException
            {
            }

            public void stop( final int options )
                throws BundleException
            {
            }

            public void stop()
                throws BundleException
            {
            }

            public void start( final int options )
                throws BundleException
            {
            }

            public void start()
                throws BundleException
            {
            }

            public Class<?> loadClass( final String name )
                throws ClassNotFoundException
            {
                return null;
            }

            public boolean hasPermission( final Object permission )
            {
                return false;
            }

            public Version getVersion()
            {
                return null;
            }

            public String getSymbolicName()
            {
                return null;
            }

            public int getState()
            {
                return 0;
            }

            public Map<?, ?> getSignerCertificates( final int signersType )
            {
                return null;
            }

            public ServiceReference[] getServicesInUse()
            {
                return null;
            }

            public URL getResource( final String name )
            {
                return null;
            }

            public ServiceReference[] getRegisteredServices()
            {
                return null;
            }

            public String getLocation()
            {
                return null;
            }

            public long getLastModified()
            {
                return 0;
            }

            public Dictionary<?, ?> getHeaders( final String locale )
            {
                return null;
            }

            public Dictionary<?, ?> getHeaders()
            {
                return null;
            }

            public Enumeration<?> getEntryPaths( final String path )
            {
                return null;
            }

            public URL getEntry( final String path )
            {
                return null;
            }

            public long getBundleId()
            {
                return 0;
            }

            public BundleContext getBundleContext()
            {
                return null;
            }

            public Enumeration<?> findEntries( final String path, final String filePattern, final boolean recurse )
            {
                return null;
            }
        } );

        assertFalse( space.getResources( "error" ).hasMoreElements() );
    }
}
