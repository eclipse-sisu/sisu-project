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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

public class DeferredClassTest
    extends TestCase
{
    URLClassLoader testLoader;

    @Override
    protected void setUp()
        throws MalformedURLException
    {
        testLoader = URLClassLoader.newInstance( new URL[] { new File( "target/test-classes" ).toURI().toURL() }, null );
    }

    private static class Dummy
    {
    }

    public void testStrongDeferredClass()
    {
        final String clazzName = Dummy.class.getName();
        final ClassSpace space = new URLClassSpace( testLoader );
        final DeferredClass<?> clazz = space.deferLoadClass( clazzName );

        assertEquals( clazzName, clazz.getName() );
        assertEquals( clazzName, clazz.load().getName() );
        assertFalse( Dummy.class.equals( clazz.load() ) );

        assertEquals( ( 17 * 31 + clazzName.hashCode() ) * 31 + space.hashCode(), clazz.hashCode() );

        assertEquals( clazz, clazz );

        assertEquals( new NamedClass<Object>( space, clazzName ), clazz );

        assertFalse( clazz.equals( new DeferredClass<Object>()
        {
            @SuppressWarnings( "unchecked" )
            public Class<Object> load()
                throws TypeNotPresentException
            {
                return (Class<Object>) clazz.load();
            }

            public String getName()
            {
                return clazz.getName();
            }

            public DeferredProvider<Object> asProvider()
            {
                throw new UnsupportedOperationException();
            }
        } ) );

        final String clazzName2 = clazzName + "$1";
        final ClassSpace space2 = new URLClassSpace( ClassLoader.getSystemClassLoader(), null );

        assertFalse( clazz.equals( new NamedClass<Object>( space, clazzName2 ) ) );
        assertFalse( clazz.equals( new NamedClass<Object>( space2, clazzName ) ) );

        assertTrue( clazz.toString().contains( clazzName ) );
        assertTrue( clazz.toString().contains( space.toString() ) );
    }

    public void testLoadedClass()
    {
        final DeferredClass<?> dummyClazz = new LoadedClass<Dummy>( Dummy.class );
        final DeferredClass<?> stringClazz = new LoadedClass<String>( String.class );

        assertEquals( String.class.getName(), stringClazz.getName() );
        assertEquals( String.class.getName(), stringClazz.load().getName() );
        assertSame( String.class, stringClazz.load() );

        assertEquals( stringClazz, stringClazz );

        assertFalse( stringClazz.equals( dummyClazz ) );
        assertFalse( stringClazz.equals( String.class ) );

        assertEquals( String.class.hashCode(), stringClazz.hashCode() );
        assertEquals( "Loaded " + String.class, stringClazz.toString() );

        assertEquals( "Loaded " + Dummy.class + " from " + Dummy.class.getClassLoader(), dummyClazz.toString() );
    }

    public void testMissingStrongDeferredClass()
    {
        try
        {
            final ClassSpace space = new URLClassSpace( testLoader );
            System.out.println( new NamedClass<Object>( space, "unknown-class" ) );
            System.out.println( new LoadedClass<Object>( getClass() ) );
            new NamedClass<Object>( space, "unknown-class" ).load();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
        }
    }
}
