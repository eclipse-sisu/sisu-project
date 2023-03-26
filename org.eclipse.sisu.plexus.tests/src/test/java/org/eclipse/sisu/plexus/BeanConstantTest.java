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
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import junit.framework.TestCase;

public class BeanConstantTest
    extends TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new AbstractModule()
        {
            private void bindBean( final String name, final String clazzName, final String content )
            {
                final String xml = "<bean implementation='" + clazzName + "'>" + content + "</bean>";
                bindConstant().annotatedWith( Names.named( name ) ).to( xml );
            }

            @Override
            protected void configure()
            {
                bindBean( "EmptyBean", EmptyBean.class.getName(), " " );
                bindBean( "MissingType", "some.unknown.type", "" );

                bindBean( "BeanWithProperty", BeanWithProperties.class.getName(), "<text/><value>4.2</value>" );
                bindBean( "MissingProperty", EmptyBean.class.getName(), " <value>4.2</value>" );

                bindBean( "MissingDefaultConstructor", MissingDefaultConstructor.class.getName(), "" );
                bindBean( "BrokenDefaultConstructor", BrokenDefaultConstructor.class.getName(), "" );
                bindBean( "MissingStringConstructor", MissingStringConstructor.class.getName(), "text" );
                bindBean( "BrokenStringConstructor", BrokenStringConstructor.class.getName(), "text" );

                bindConstant().annotatedWith( Names.named( "README" ) ).to( "some/temp/readme.txt" );
                bindConstant().annotatedWith( Names.named( "SITE" ) ).to( "http://www.sonatype.org" );
                bindConstant().annotatedWith( Names.named( "DATE" ) ).to( "2009-11-15 18:02:00" );

                install( new PlexusDateTypeConverter() );
                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
                install( new ConfigurationConverter() );
            }
        } ).injectMembers( this );
    }

    static class EmptyBean
    {
    }

    static class BeanWithProperties
    {
        float value;

        void setValue( final float _value )
        {
            value = _value;
        }

        String text;
    }

    static class MissingDefaultConstructor
    {
        private MissingDefaultConstructor()
        {
        }
    }

    static class BrokenDefaultConstructor
    {
        public BrokenDefaultConstructor()
        {
            throw new RuntimeException();
        }
    }

    static class MissingStringConstructor
    {
    }

    static class BrokenStringConstructor
    {
        public BrokenStringConstructor( final String text )
        {
            throw new RuntimeException( text );
        }
    }

    @Inject
    Injector injector;

    public void testEmptyBeanConversion()
    {
        assertEquals( EmptyBean.class, getBean( "EmptyBean", Object.class ).getClass() );
    }

    public void testMissingType()
    {
        testFailedConversion( "MissingType", EmptyBean.class );
    }

    public void testPeerClassLoader1()
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( null );
            assertEquals( EmptyBean.class, getBean( "EmptyBean", EmptyBean.class ).getClass() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    public void testPeerClassLoader2()
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( null );
            assertEquals( EmptyBean.class, getBean( "EmptyBean", Object.class ).getClass() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    public void testBeanWithPropertiesConversion()
    {
        final BeanWithProperties beanWithProperties = (BeanWithProperties) getBean( "BeanWithProperty", Object.class );
        assertEquals( 4.2f, beanWithProperties.value, 0f );
        assertEquals( beanWithProperties.text.length(), 0 );
    }

    public void testMissingPropertyConversion()
    {
        testFailedConversion( "MissingProperty", Object.class );
    }

    public void testStringXMLConversion()
    {
        final PlexusBeanConverter configurator = injector.getInstance( PlexusBeanConverter.class );
        assertEquals( configurator.convert( TypeLiteral.get( String.class ), "<one><two/></one>" ).length(), 0 );
    }

    public void testMissingDefaultConstructor()
    {
        testFailedConversion( "MissingDefaultConstructor", Object.class );
    }

    public void testBrokenDefaultConstructor()
    {
        testFailedConversion( "BrokenDefaultConstructor", Object.class );
    }

    public void testMissingStringConstructor()
    {
        testFailedConversion( "MissingStringConstructor", Object.class );
    }

    public void testBrokenStringConstructor()
    {
        testFailedConversion( "BrokenStringConstructor", Object.class );
    }

    public void testSimpleFileBean()
    {
        assertEquals( "readme.txt", injector.getInstance( Key.get( File.class, Names.named( "README" ) ) ).getName() );
    }

    public void testSimpleUrlBean()
    {
        assertEquals( "www.sonatype.org",
                      injector.getInstance( Key.get( URL.class, Names.named( "SITE" ) ) ).getHost() );
    }

    public void testNonBean()
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime( injector.getInstance( Key.get( Date.class, Names.named( "DATE" ) ) ) );
        assertEquals( 15, calendar.get( Calendar.DAY_OF_MONTH ) );
        assertEquals( Calendar.NOVEMBER, calendar.get( Calendar.MONTH ) );
        assertEquals( 2009, calendar.get( Calendar.YEAR ) );
    }

    @SuppressWarnings( "boxing" )
    public void testConfigurator()
    {
        final PlexusBeanConverter configurator = injector.getInstance( PlexusBeanConverter.class );
        final float value = configurator.convert( TypeLiteral.get( float.class ), "4.2" );
        assertEquals( 4.2f, value, 0 );
    }

    private Object getBean( final String bindingName, final Class<?> clazz )
    {
        return injector.getInstance( Key.get( clazz, Names.named( bindingName ) ) );
    }

    private void testFailedConversion( final String bindingName, final Class<?> clazz )
    {
        try
        {
            getBean( bindingName, clazz );
            fail( "Expected ConfigurationException" );
        }
        catch ( final ConfigurationException e )
        {
        }
    }
}
