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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;

import junit.framework.TestCase;

public class MapConstantTest
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
                      "<items implementation='java.util.LinkedHashMap'>"
                          + "<builder implementation='java.lang.StringBuilder'>TEST</builder>"
                          + "<file implementation='java.io.File'>TEST</file>" + "</items>" );

                bind( "Map", "<items><key1>value1</key1><key2>value2</key2></items>" );

                bind( "Properties", "<items><property><name>key1</name><value>value1</value></property>"
                    + "<property><value>value2</value><name>key2</name></property></items>" );

                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
                install( new ConfigurationConverter() );
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Empty" )
    Map<?, ?> empty;

    @Inject
    @Named( "Custom" )
    Map<?, ?> custom;

    @Inject
    @Named( "Map" )
    Map<?, ?> map;

    @Inject
    @Named( "Properties" )
    Properties properties;

    public void testEmptyMap()
    {
        assertTrue( empty.isEmpty() );
    }

    public void testCustomMap()
    {
        assertEquals( LinkedHashMap.class, custom.getClass() );
        assertEquals( "TEST", custom.get( "builder" ).toString() );
        assertEquals( StringBuilder.class, custom.get( "builder" ).getClass() );
        assertEquals( new File( "TEST" ), custom.get( "file" ) );
    }

    public void testMapAndProperties()
    {
        final HashMap<String, String> testMap = new HashMap<String, String>();
        testMap.put( "key1", "value1" );
        testMap.put( "key2", "value2" );

        assertEquals( testMap, map );
        assertEquals( testMap, properties );
    }
}
