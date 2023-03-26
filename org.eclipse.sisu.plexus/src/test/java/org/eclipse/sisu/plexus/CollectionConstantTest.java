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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;

import junit.framework.TestCase;

public class CollectionConstantTest
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

                bind( "Custom", "<items implementation='java.util.LinkedHashSet'>"
                    + "<item implementation='java.io.File'>FOO</item><item>BAR</item></items>" );

                bind( "Animals",
                      "<animals><animal>cat</animal><animal>dog</animal><animal>aardvark</animal></animals>" );

                bind( "Numbers", "<as><a><b>1</b><b>2</b></a><a><b>3</b><b>4</b></a><a><b>5</b><b>6</b></a></as>" );

                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
                install( new ConfigurationConverter() );
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Empty" )
    List<?> empty;

    @Inject
    @Named( "Custom" )
    Set<?> custom;

    @Inject
    @Named( "Animals" )
    Collection<?> animals;

    @Inject
    @Named( "Numbers" )
    Collection<Collection<Integer>> numbers;

    public void testEmptyCollection()
    {
        assertTrue( empty.isEmpty() );
    }

    public void testCustomCollections()
    {
        assertEquals( LinkedHashSet.class, custom.getClass() );
        final Iterator<?> i = custom.iterator();
        assertEquals( new File( "FOO" ), i.next() );
        assertEquals( "BAR", i.next() );
        assertFalse( i.hasNext() );
    }

    public void testStringCollection()
    {
        assertEquals( Arrays.asList( "cat", "dog", "aardvark" ), animals );
    }

    @SuppressWarnings( { "unchecked", "boxing" } )
    public void testPrimitiveCollection()
    {
        assertEquals( Arrays.asList( Arrays.asList( 1, 2 ), Arrays.asList( 3, 4 ), Arrays.asList( 5, 6 ) ), numbers );
    }
}
