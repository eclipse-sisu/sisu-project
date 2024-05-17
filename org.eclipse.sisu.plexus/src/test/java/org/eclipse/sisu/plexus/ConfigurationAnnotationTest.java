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
package org.eclipse.sisu.plexus;

import java.util.Arrays;
import java.util.HashSet;

import org.codehaus.plexus.component.annotations.Configuration;

import junit.framework.TestCase;

public class ConfigurationAnnotationTest
    extends TestCase
{
    @Configuration( "Default" )
    String defaultConfig;

    @Configuration( name = "Name", value = "Default" )
    String namedConfig;

    @Configuration( "${property}" )
    String propertyConfig;

    public void testConfigurationImpl()
        throws NoSuchFieldException
    {
        checkBehaviour( "defaultConfig" );
        checkBehaviour( "namedConfig" );
        checkBehaviour( "propertyConfig" );

        assertFalse( replicate( getConfiguration( "defaultConfig" ) ).equals( getConfiguration( "namedConfig" ) ) );
        assertFalse( replicate( getConfiguration( "defaultConfig" ) ).equals( getConfiguration( "propertyConfig" ) ) );
    }

    private static void checkBehaviour( final String name )
        throws NoSuchFieldException
    {
        final Configuration orig = getConfiguration( name );
        final Configuration clone = replicate( orig );

        assertTrue( orig.equals( clone ) );
        assertTrue( clone.equals( orig ) );
        assertTrue( clone.equals( clone ) );
        assertFalse( clone.equals( "" ) );

        assertEquals( orig.hashCode(), clone.hashCode() );

        String origToString = orig.toString().replace( "\"", "" ).replace( ".class", "" );
        String cloneToString = clone.toString().replace( '[', '{' ).replace( ']', '}' );
        cloneToString = cloneToString.replace( "class ", "" ).replace( "interface ", "" );

        assertEquals( new HashSet<String>( Arrays.asList( origToString.split( "[(, )]" ) ) ),
                      new HashSet<String>( Arrays.asList( cloneToString.split( "[(, )]" ) ) ) );

        assertEquals( orig.annotationType(), clone.annotationType() );
    }

    private static Configuration getConfiguration( final String name )
        throws NoSuchFieldException
    {
        return ConfigurationAnnotationTest.class.getDeclaredField( name ).getAnnotation( Configuration.class );
    }

    private static Configuration replicate( final Configuration orig )
    {
        return new ConfigurationImpl( orig.name(), orig.value() );
    }

    public void testNullChecks()
    {
        checkNullNotAllowed( null, "" );
        checkNullNotAllowed( "", null );
    }

    private static void checkNullNotAllowed( final String name, final String value )
    {
        try
        {
            new ConfigurationImpl( name, value );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( final IllegalArgumentException e )
        {
        }
    }
}
