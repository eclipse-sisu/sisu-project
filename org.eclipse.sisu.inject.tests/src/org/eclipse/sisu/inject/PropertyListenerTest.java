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
package org.eclipse.sisu.inject;

import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.sisu.reflect.BeanProperty;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;

public class PropertyListenerTest
    extends TestCase
{
    static class Base
    {
        String a;
    }

    static class Bean0
        extends Base
    {
        String last;
    }

    static class Bean1
        extends Base
    {
        String b;

        Bean2 bean;
    }

    static class Bean2
        extends Base
    {
        String b;

        String last;

        String c;

        String ignore;

        @Named( "injected" )
        @javax.inject.Inject
        int jsr330;

        @Named( "injected" )
        @com.google.inject.Inject
        int guice;

        String d;
    }

    static class Bean3
        extends Base
    {
        String b;

        String error;

        String c;
    }

    static class Bean4
        extends Bean1
    {
        void setA( @SuppressWarnings( "unused" ) final String unused )
        {
            assertNull( a ); // hidden by our setter method
            assertNotNull( b ); // should be injected first
            a = "correct order";
        }
    }

    class NamedPropertyBinder
        implements PropertyBinder
    {
        public <T> PropertyBinding bindProperty( final BeanProperty<T> property )
        {
            if ( "last".equals( property.getName() ) )
            {
                return PropertyBinder.LAST_BINDING;
            }
            if ( "ignore".equals( property.getName() ) )
            {
                return null;
            }
            if ( "error".equals( property.getName() ) )
            {
                throw new RuntimeException( "Broken binding" );
            }
            if ( "bean".equals( property.getName() ) )
            {
                return new PropertyBinding()
                {
                    public void injectProperty( final Object bean )
                    {
                        property.set( bean, injector.getInstance( Key.get( property.getType() ) ) );
                    }
                };
            }
            return new PropertyBinding()
            {
                @SuppressWarnings( "unchecked" )
                public void injectProperty( final Object bean )
                {
                    property.set( bean, (T) ( property.getName() + "Value" ) );
                }
            };
        }
    }

    final PropertyBinder namedPropertyBinder = new NamedPropertyBinder();

    Injector injector;

    @Override
    public void setUp()
    {
        injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindListener( new AbstractMatcher<TypeLiteral<?>>()
                {
                    public boolean matches( final TypeLiteral<?> type )
                    {
                        return Base.class.isAssignableFrom( type.getRawType() );
                    }
                }, new BeanListener( new BeanBinder()
                {
                    public <T> PropertyBinder bindBean( final TypeLiteral<T> type, final TypeEncounter<T> encounter )
                    {
                        return type.getRawType().getName().contains( "Bean" ) ? namedPropertyBinder : null;
                    }
                } ) );

                bindConstant().annotatedWith( Names.named( "injected" ) ).to( 42 );
            }
        } );
    }

    public void testNoBean()
    {
        final Base base = injector.getInstance( Base.class );
        assertNull( base.a );
    }

    public void testNoBindings()
    {
        final Bean0 bean0 = injector.getInstance( Bean0.class );
        assertNull( bean0.a );
    }

    public void testPropertyBindings()
    {
        final Bean1 bean1 = injector.getInstance( Bean1.class );
        assertEquals( "bValue", bean1.b );
        assertEquals( "aValue", bean1.a );
    }

    public void testSpecialProperties()
    {
        final Bean2 bean2 = injector.getInstance( Bean2.class );
        assertEquals( "dValue", bean2.d );
        assertEquals( 42, bean2.guice );
        assertEquals( 42, bean2.jsr330 );
        assertEquals( "cValue", bean2.c );
        assertNull( bean2.b );
        assertNull( bean2.a );

        try
        {
            PropertyBinder.LAST_BINDING.injectProperty( bean2 );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testBrokenBinding()
    {
        try
        {
            injector.getInstance( Bean3.class );
            fail( "Expected ConfigurationException" );
        }
        catch ( final ConfigurationException e )
        {
            e.printStackTrace();
        }
    }

    public void testInjectionOrder()
    {
        assertEquals( "correct order", injector.getInstance( Bean4.class ).a );
    }
}
