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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.bean.BeanProperty;
import org.eclipse.sisu.bean.PropertyBinding;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

public class PlexusConfigurationTest
    extends TestCase
{
    @Inject
    ConfiguredComponent component;

    @Inject
    Injector injector;

    static class ComponentManager
        implements BeanManager
    {
        static int SEEN;

        public boolean manage( final Class<?> clazz )
        {
            return ConfiguredComponent.class.isAssignableFrom( clazz );
        }

        public PropertyBinding manage( final BeanProperty<?> property )
        {
            return null;
        }

        public boolean manage( final Object bean )
        {
            SEEN++;
            return true;
        }

        public boolean unmanage( final Object bean )
        {
            return false;
        }

        public boolean unmanage()
        {
            return false;
        }
    }

    @Override
    protected void setUp()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                install( new PlexusDateTypeConverter() );

                bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );

                install( new PlexusBindingModule( new ComponentManager(),
                                                  new PlexusAnnotatedBeanModule( null, null ) ) );

                requestInjection( PlexusConfigurationTest.this );
            }
        } );
    }

    @Component( role = Object.class )
    static class ConfiguredComponent
    {
        @Configuration( "1" )
        String a;

        @Configuration( "2" )
        Integer b;

        @Configuration( "3" )
        int c;

        @Configuration( "4" )
        Double d;

        @Configuration( "5" )
        double e;

        @Configuration( "<map>" + //
            "<key1><a><x><elem1>true</elem1><elem2>false</elem2></x></a></key1>" + //
            "<key2><b><y><elem1>false</elem1><elem2>true</elem2></y></b></key2>" + //
            "</map>" )
        Map<String, Map<String, Map<String, List<Boolean>>>> map;

        @Configuration( "<container><xml><element/></xml></container>" )
        XmlContainerComponent xmlContainer;
    }

    @Component( role = Object.class )
    static class MisconfiguredComponent
    {
        @Configuration( "misconfigured" )
        SomeBean bean;
    }

    public static class SomeBean
    {
        public SomeBean( final String data )
        {
            if ( "misconfigured".equals( data ) )
            {
                throw new NoClassDefFoundError();
            }
        }
    }

    public static class XmlContainerComponent
    {
        Xpp3Dom xml;
    }

    public void testConfiguration()
    {
        assertEquals( "1", component.a );
        assertEquals( Integer.valueOf( 2 ), component.b );
        assertEquals( 3, component.c );
        assertEquals( Double.valueOf( 4.0 ), component.d );
        assertEquals( 5.0, component.e, 0 );

        Map<String, Map<String, Map<String, List<Boolean>>>> expectedMap = new HashMap<>();
        expectedMap.put( "key1", singletonMap( "a", singletonMap( "x", asList( true, false ) ) ) );
        expectedMap.put( "key2", singletonMap( "b", singletonMap( "y", asList( false, true ) ) ) );
        assertEquals( expectedMap, component.map );

        assertNotNull( component.xmlContainer );
        assertNotNull( component.xmlContainer.xml );
        assertEquals( "xml", component.xmlContainer.xml.getName() );
        assertEquals( 1, component.xmlContainer.xml.getChildCount() );
        assertEquals( "element", component.xmlContainer.xml.getChild( 0 ).getName() );

        assertEquals( 1, ComponentManager.SEEN );

        final ConfiguredComponent jitComponent = injector.getInstance( ConfiguredComponent.class );

        assertEquals( "1", jitComponent.a );
        assertEquals( Integer.valueOf( 2 ), jitComponent.b );
        assertEquals( 3, jitComponent.c );
        assertEquals( Double.valueOf( 4.0 ), jitComponent.d );
        assertEquals( 5.0, jitComponent.e, 0 );

        assertEquals( 2, ComponentManager.SEEN );

        try
        {
            injector.getInstance( MisconfiguredComponent.class );
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        assertEquals( 2, ComponentManager.SEEN );
    }
}
