/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.jar.Manifest;

import org.apache.felix.framework.FrameworkFactory;
import org.eclipse.sisu.BaseTests;
import org.eclipse.sisu.inject.DeferredClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@BaseTests
class BundleClassSpaceTest
{
    private static final URL EMPTY_BUNDLE = ZipEntryIteratorTest.class.getResource( "empty.jar" );

    private static final URL SIMPLE_BUNDLE = ZipEntryIteratorTest.class.getResource( "simple_bundle.jar" );

    private static final URL LOGGING_BUNDLE = ZipEntryIteratorTest.class.getResource( "logging_bundle.jar" );

    private Framework framework;

    @BeforeEach
    void setUp()
        throws Exception
    {

        final Map<String, String> configuration = new HashMap<String, String>();
        configuration.put( Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT );
        configuration.put( Constants.FRAMEWORK_STORAGE, "target/bundlecache" );

        final FrameworkFactory frameworkFactory = new FrameworkFactory();
        framework = frameworkFactory.newFramework( configuration );
        framework.start();
    }

    @AfterEach
    void tearDown()
        throws Exception
    {
        framework.stop();
        framework.waitForStop( 0 );
    }

    @Test
    void testHashCodeAndEquals()
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

    @Test
    void testClassSpaceResources()
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

    @Test
    void testDeferredClass()
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

    @Test
    void testBrokenResources()
    {
        final InvocationHandler handler = new InvocationHandler()
        {
            public Object invoke( final Object proxy, final Method method, final Object[] args )
                throws Throwable
            {
                throw new IOException();
            }
        };

        final ClassSpace space =
            new BundleClassSpace( (Bundle) Proxy.newProxyInstance( Bundle.class.getClassLoader(),
                                                                   new Class<?>[] { Bundle.class }, handler ) );

        assertFalse( space.getResources( "error" ).hasMoreElements() );
    }
}
