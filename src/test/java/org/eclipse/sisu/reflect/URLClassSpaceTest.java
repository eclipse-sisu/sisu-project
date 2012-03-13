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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.eclipse.sisu.reflect.oops.Handler;

public class URLClassSpaceTest
    extends TestCase
{
    private static final URL SIMPLE_JAR = URLClassSpaceTest.class.getResource( "simple.jar" );

    private static final URL CLASS_PATH_JAR = URLClassSpaceTest.class.getResource( "class path.jar" );

    private static final URL COMMONS_LOGGING_JAR = URLClassSpaceTest.class.getResource( "commons-logging-1.1.1.jar" );

    private static final URL CORRUPT_MANIFEST = URLClassSpaceTest.class.getResource( "corrupt.manifest/" );

    private static final URL BROKEN_JAR = URLClassSpaceTest.class.getResource( "broken.jar" );

    private static final URL NESTED_WAR = URLClassSpaceTest.class.getResource( "nested.war" );

    public void testHashCodeAndEquals()
    {
        final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
        final ClassSpace space = new URLClassSpace( systemLoader, null );

        assertEquals( space, space );

        assertEquals( space, new URLClassSpace( systemLoader, new URL[] { SIMPLE_JAR } ) );

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

        assertEquals( systemLoader.hashCode(), space.hashCode() );
        assertEquals( systemLoader.toString(), space.toString() );
    }

    public void testClassSpaceResources()
        throws IOException
    {
        final ClassSpace space = new URLClassSpace( URLClassLoader.newInstance( new URL[] { COMMONS_LOGGING_JAR } ) );
        Enumeration<URL> e;

        int n = 0;
        e = space.getResources( "META-INF/MANIFEST.MF" );
        while ( true )
        {
            n++;

            // should have several matches from parent loader, local match should be last
            if ( e.nextElement().getPath().startsWith( COMMONS_LOGGING_JAR.toString() ) )
            {
                assertFalse( e.hasMoreElements() );
                break;
            }
        }
        assertTrue( n > 1 );

        e = space.findEntries( "META-INF", "*.MF", false );

        // only expect to see single result
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( COMMONS_LOGGING_JAR.toString() ) );
        assertFalse( e.hasMoreElements() );

        final URL manifestURL = space.getResource( "META-INF/MANIFEST.MF" );
        assertNotNull( manifestURL );
        new Manifest( manifestURL.openStream() );
    }

    public void testClassPathExpansion()
        throws IOException
    {
        // System.setProperty( "java.protocol.handler.pkgs", getClass().getPackage().getName() );
        URL.setURLStreamHandlerFactory( new URLStreamHandlerFactory()
        {
            public URLStreamHandler createURLStreamHandler( final String protocol )
            {
                if ( "oops".equals( protocol ) )
                {
                    return new Handler();
                }
                return null;
            }
        } );

        final URLClassSpace space =
            new URLClassSpace( URLClassLoader.newInstance( new URL[] { SIMPLE_JAR, CLASS_PATH_JAR, null,
                new URL( "oops:bad/" ), CLASS_PATH_JAR, CORRUPT_MANIFEST } ) );

        final Enumeration<URL> e = space.findEntries( "META-INF", "*.MF", false );

        // expect to see three results
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( SIMPLE_JAR.toString() ) );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( CLASS_PATH_JAR.toString() ) );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( COMMONS_LOGGING_JAR.toString() ) );
        assertFalse( e.hasMoreElements() );

        assertTrue( Arrays.equals( new URL[] { SIMPLE_JAR, CLASS_PATH_JAR, new URL( "oops:bad/" ), CORRUPT_MANIFEST,
            BROKEN_JAR, COMMONS_LOGGING_JAR }, space.getURLs() ) );
    }

    public void testNullSearchPath()
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader(), null );
        final Enumeration<URL> e = space.findEntries( null, null, true );

        // local search should see nothing
        assertFalse( e.hasMoreElements() );
    }

    public void testEmptySearchPath()
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader(), new URL[0] );
        final Enumeration<URL> e = space.findEntries( null, null, true );

        // local search should see nothing
        assertFalse( e.hasMoreElements() );
    }

    public void testBrokenResources()
    {
        final ClassSpace space = new URLClassSpace( new ClassLoader()
        {
            @Override
            public Enumeration<URL> getResources( final String name )
                throws IOException
            {
                throw new IOException();
            }
        } );

        // should see nothing, and not throw any exceptions
        assertFalse( space.getResources( "error" ).hasMoreElements() );
    }

    public void testClassPathDetection()
    {
        final ClassLoader parent = URLClassLoader.newInstance( new URL[] { CLASS_PATH_JAR } );
        final ClassLoader child = URLClassLoader.newInstance( new URL[0], parent );
        final ClassLoader grandchild = new URLClassLoader( new URL[0], child )
        {
            @Override
            public URL[] getURLs()
            {
                return null;
            }
        };

        final Enumeration<URL> e = new URLClassSpace( new ClassLoader( grandchild )
        {
        } ).findEntries( "META-INF", "*.MF", false );

        // expect to see three results
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( CLASS_PATH_JAR.toString() ) );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( COMMONS_LOGGING_JAR.toString() ) );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( SIMPLE_JAR.toString() ) );
        assertFalse( e.hasMoreElements() );

        final ClassLoader orphan = URLClassLoader.newInstance( new URL[0], null );

        // expect to see no results
        assertFalse( new URLClassSpace( orphan ).findEntries( "META-INF", "*.MF", false ).hasMoreElements() );
    }

    public void testJarProtocol()
        throws MalformedURLException
    {
        final URLClassSpace space =
            new URLClassSpace( URLClassLoader.newInstance( new URL[] { new URL( "jar:" + CLASS_PATH_JAR + "!/" ) } ) );

        final Enumeration<URL> e = space.findEntries( "META-INF", "*.MF", false );

        // expect to see three results
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( CLASS_PATH_JAR.toString() ) );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( COMMONS_LOGGING_JAR.toString() ) );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().getPath().startsWith( SIMPLE_JAR.toString() ) );
        assertFalse( e.hasMoreElements() );
    }

    public void testNestedWar()
        throws MalformedURLException
    {
        final URLClassSpace space =
            new URLClassSpace( URLClassLoader.newInstance( new URL[] {
                new URL( "jar:" + NESTED_WAR + "!/WEB-INF/classes/" ),
                new URL( "jar:" + NESTED_WAR + "!/WEB-INF/lib/commons-logging-1.1.1.jar" ) } ) );

        Enumeration<URL> e = space.findEntries( "META-INF", "*.MF", false );

        // expect to see one result
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().toString().endsWith( "/nested.war!/WEB-INF/lib/commons-logging-1.1.1.jar#META-INF/MANIFEST.MF" ) );
        assertFalse( e.hasMoreElements() );

        e = space.findEntries( null, "Log.class", true );

        // only one result, as can't "glob" embedded directory
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().toString().endsWith( "/nested.war!/WEB-INF/lib/commons-logging-1.1.1.jar#org/apache/commons/logging/Log.class" ) );
        assertFalse( e.hasMoreElements() );

        e = space.findEntries( "org/apache/commons/logging", "Log.class", false );

        // can see both results, as using non-"globbed" search
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().toString().endsWith( "/nested.war!/WEB-INF/classes/org/apache/commons/logging/Log.class" ) );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().toString().endsWith( "/nested.war!/WEB-INF/lib/commons-logging-1.1.1.jar#org/apache/commons/logging/Log.class" ) );
        assertFalse( e.hasMoreElements() );

        e = space.findEntries( null, "missing", true );
        assertFalse( e.hasMoreElements() );
    }
}
