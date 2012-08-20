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
package org.eclipse.sisu.plexus.binders;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.inject.PropertyBinding;
import org.eclipse.sisu.plexus.annotations.ConfigurationImpl;
import org.eclipse.sisu.plexus.annotations.RequirementImpl;
import org.eclipse.sisu.plexus.binders.PlexusBeanManager;
import org.eclipse.sisu.plexus.binders.PlexusBindingModule;
import org.eclipse.sisu.plexus.config.PlexusBeanConverter;
import org.eclipse.sisu.plexus.config.PlexusBeanLocator;
import org.eclipse.sisu.plexus.config.PlexusBeanMetadata;
import org.eclipse.sisu.plexus.config.PlexusBeanModule;
import org.eclipse.sisu.plexus.config.PlexusBeanSource;
import org.eclipse.sisu.plexus.converters.PlexusDateTypeConverter;
import org.eclipse.sisu.plexus.converters.PlexusXmlBeanConverter;
import org.eclipse.sisu.plexus.locators.DefaultPlexusBeanLocator;
import org.eclipse.sisu.reflect.BeanProperty;
import org.eclipse.sisu.reflect.DeferredClass;
import org.eclipse.sisu.reflect.URLClassSpace;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class PlexusBeanMetadataTest
    extends TestCase
{
    @Inject
    @Named( "2" )
    Bean bean;

    @Inject
    Injector injector;

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

                bindConstant().annotatedWith( Names.named( "KEY1" ) ).to( "REQUIREMENT" );

                final PlexusBeanManager manager = new TestBeanManager();

                install( new PlexusBindingModule( null, new BeanSourceA() ) );
                install( new PlexusBindingModule( manager, new BeanSourceB() ) );
                install( new PlexusBindingModule( null, new BeanSourceC() ) );
                install( new PlexusBindingModule( null, new CustomizedBeanSource() ) );

                requestInjection( PlexusBeanMetadataTest.this );
            }
        } );
    }

    static class TestBeanManager
        implements PlexusBeanManager
    {
        public boolean manage( final Class<?> clazz )
        {
            return false;
        }

        public PropertyBinding manage( final BeanProperty<?> property )
        {
            return null;
        }

        public boolean manage( final Object bean )
        {
            return false;
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

    interface Bean
    {
        Object getExtraMetadata();

        void setExtraMetadata( Object metadata );
    }

    static class DefaultBean1
        implements Bean
    {
        Object extraMetadata;

        public Object getExtraMetadata()
        {
            return extraMetadata;
        }

        public void setExtraMetadata( final Object metadata )
        {
            extraMetadata = metadata;
        }
    }

    static class DefaultBean2
    {
        String extraMetadata;

        String leftovers;
    }

    static class BeanSourceA
        implements PlexusBeanModule
    {
        public PlexusBeanSource configure( final Binder binder )
        {
            binder.withSource( "A" ).bind( Bean.class ).annotatedWith( Names.named( "2" ) ).to( DefaultBean1.class ).asEagerSingleton();
            return null;
        }
    }

    static class BeanSourceB
        implements PlexusBeanModule
    {
        public PlexusBeanSource configure( final Binder binder )
        {
            binder.withSource( "B" ).bind( DefaultBean2.class );
            return null;
        }
    }

    static class BeanSourceC
        implements PlexusBeanModule
    {
        public PlexusBeanSource configure( final Binder binder )
        {
            binder.withSource( "C" ).bind( DefaultBean2.class ).annotatedWith( Names.named( "2" ) ).to( DefaultBean2.class );
            return null;
        }
    }

    static class CustomizedBeanSource
        implements PlexusBeanModule, PlexusBeanSource
    {
        public PlexusBeanSource configure( final Binder binder )
        {
            return this;
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            if ( DefaultBean1.class.equals( implementation ) )
            {
                return new PlexusBeanMetadata()
                {
                    public boolean isEmpty()
                    {
                        return false;
                    }

                    @SuppressWarnings( "deprecation" )
                    public Requirement getRequirement( final BeanProperty<?> property )
                    {
                        if ( "extraMetadata".equals( property.getName() ) )
                        {
                            return new RequirementImpl( String.class, false, "KEY1" );
                        }
                        return null;
                    }

                    public Configuration getConfiguration( final BeanProperty<?> property )
                    {
                        return null;
                    }
                };
            }
            if ( DefaultBean2.class.equals( implementation ) )
            {
                return new PlexusBeanMetadata()
                {
                    private boolean used = false;

                    public boolean isEmpty()
                    {
                        return used;
                    }

                    public Requirement getRequirement( final BeanProperty<?> property )
                    {
                        return null;
                    }

                    public Configuration getConfiguration( final BeanProperty<?> property )
                    {
                        if ( "extraMetadata".equals( property.getName() ) )
                        {
                            used = true;

                            return new ConfigurationImpl( "KEY2", "CONFIGURATION" );
                        }
                        return null;
                    }
                };
            }
            return null;
        }
    }

    public void testExtraMetadata()
    {
        assertEquals( "REQUIREMENT", bean.getExtraMetadata() );
        assertEquals( "CONFIGURATION", injector.getInstance( DefaultBean2.class ).extraMetadata );
        assertSame( bean, injector.getInstance( Key.get( Bean.class, Names.named( "2" ) ) ) );
    }

    static DeferredClass<?> defer( final Class<?> clazz )
    {
        return new URLClassSpace( TestCase.class.getClassLoader() ).deferLoadClass( clazz.getName() );
    }
}
