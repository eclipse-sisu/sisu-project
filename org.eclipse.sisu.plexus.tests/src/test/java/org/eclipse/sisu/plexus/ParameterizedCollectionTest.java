/*******************************************************************************
 * Copyright (c) 2021-present Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

public class ParameterizedCollectionTest
    extends TestCase
{
    static class MapHolder
    {
        Map<String, Map<String, Map<String, List<Boolean>>>> map;
    }

    static class ListHolder
    {
        List<Map<String, Map<String, List<Boolean>>>> list;
    }

    static class ArrayHolder
    {
        Map<String, Map<String, List<Boolean>>>[] array;
    }

    public void testParameterizedMap()
        throws Exception
    {
        final MapHolder mapHolder = new MapHolder();

        configure( mapHolder, "<configuration><map>" + //
            "<key1><a><x><elem1>true</elem1><elem2>false</elem2></x></a></key1>" + //
            "<key2><b><y><elem1>false</elem1><elem2>true</elem2></y></b></key2>" + //
            "</map></configuration>" );

        Map<String, Map<String, Map<String, List<Boolean>>>> expectedMap = new HashMap<>();
        expectedMap.put( "key1", singletonMap( "a", singletonMap( "x", asList( true, false ) ) ) );
        expectedMap.put( "key2", singletonMap( "b", singletonMap( "y", asList( false, true ) ) ) );
        assertEquals( expectedMap, mapHolder.map );
    }

    public void testParameterizedList()
        throws Exception
    {
        final ListHolder listHolder = new ListHolder();

        configure( listHolder, "<configuration><list>" + //
            "<elem><a><x><elem1>true</elem1><elem2>false</elem2></x></a></elem>" + //
            "<elem><b><y><elem1>false</elem1><elem2>true</elem2></y></b></elem>" + //
            "</list></configuration>" );

        List<Map<String, Map<String, List<Boolean>>>> expectedList = new ArrayList<>();
        expectedList.add( singletonMap( "a", singletonMap( "x", asList( true, false ) ) ) );
        expectedList.add( singletonMap( "b", singletonMap( "y", asList( false, true ) ) ) );
        assertEquals( expectedList, listHolder.list );
    }

    public void testParameterizedArray()
        throws Exception
    {
        final ArrayHolder arrayHolder = new ArrayHolder();

        configure( arrayHolder, "<configuration><array>" + //
            "<elem><a><x><elem1>true</elem1><elem2>false</elem2></x></a></elem>" + //
            "<elem><b><y><elem1>false</elem1><elem2>true</elem2></y></b></elem>" + //
            "</array></configuration>" );

        Map<String, Map<String, List<Boolean>>>[] expectedArray =
            new Map[] { singletonMap( "a", singletonMap( "x", asList( true, false ) ) ),
                singletonMap( "b", singletonMap( "y", asList( false, true ) ) ) };
        assertEquals( 2, arrayHolder.array.length );
        assertEquals( expectedArray[0], arrayHolder.array[0] );
        assertEquals( expectedArray[1], arrayHolder.array[1] );
    }

    protected void configure( final Object component, final String xml )
        throws Exception
    {
        final Xpp3Dom dom = Xpp3DomBuilder.build( new StringReader( xml ) );
        final PlexusConfiguration config = new XmlPlexusConfiguration( dom );
        final ComponentConfigurator configurator = new BasicComponentConfigurator();
        final ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();
        configurator.configureComponent( component, config, evaluator, null );
    }
}
