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

import java.net.URI;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;

import junit.framework.TestCase;

public class ArrayConstantTest
    extends TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new AbstractModule()
        {
            private void bind( final String name, final String value )
            {
                bindConstant().annotatedWith( Names.named( name ) ).to( value );
            }

            @Override
            protected void configure()
            {
                bind( "Empty", "<items/>" );

                bind( "Custom",
                      "<items><item implementation='java.lang.Boolean'>true</item>"
                          + "<item implementation='java.net.URI'>file:temp</item>"
                          + "<item implementation='java.lang.Float'>8.1</item></items>" );

                bind( "Text", "<items><item>1</item><item>2</item><item>3</item></items>" );

                bind( "Numbers", "<items><item>4</item><item>5</item><item>6</item></items>" );

                bind( "Multi", "<as><a><b>1</b><b>2</b></a><a><b>3</b><b>4</b></a><a><b>5</b><b>6</b></a></as>" );

                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
                install( new ConfigurationConverter() );
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Empty" )
    char[] empty;

    @Inject
    @Named( "Custom" )
    Object[] custom;

    @Inject
    @Named( "Text" )
    String[] text;

    @Inject
    @Named( "Numbers" )
    int[] numbers;

    @Inject
    @Named( "Multi" )
    Integer[][] multi1;

    @Inject
    @Named( "Multi" )
    double[][] multi2;

    public void testEmptyArray()
    {
        assertEquals( 0, empty.length );
    }

    @SuppressWarnings( "boxing" )
    public void testCustomArray()
    {
        assertTrue( Arrays.equals( new Object[] { true, URI.create( "file:temp" ), 8.1f }, custom ) );
    }

    public void testStringArray()
    {
        assertTrue( Arrays.equals( new String[] { "1", "2", "3" }, text ) );
    }

    public void testPrimitiveArray()
    {
        assertTrue( Arrays.equals( new int[] { 4, 5, 6 }, numbers ) );
    }

    @SuppressWarnings( "boxing" )
    public void testMultiArrays()
    {
        assertTrue( Arrays.deepEquals( new Integer[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } }, multi1 ) );
        assertTrue( Arrays.deepEquals( new double[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } }, multi2 ) );
    }
}
