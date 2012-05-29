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
package org.eclipse.sisu.locators;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.eclipse.sisu.locators.RankedBindingsTest.Bean;
import org.eclipse.sisu.locators.RankedBindingsTest.BeanImpl;
import org.eclipse.sisu.locators.RankedBindingsTest.BeanImpl2;
import org.eclipse.sisu.locators.spi.BindingPublisher;
import org.eclipse.sisu.locators.spi.BindingSubscriber;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class DefaultBeanLocatorTest
    extends TestCase
{
    Injector parent;

    Injector child1;

    Injector child2;

    Injector child3;

    Injector child4;

    @Override
    public void setUp()
        throws Exception
    {
        parent = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "A" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "-" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "Z" ) ).to( BeanImpl.class );
            }
        } );

        child1 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M1" ) ).to( BeanImpl.class );
                bind( Bean.class ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "N1" ) ).to( BeanImpl.class );
            }
        } );

        child2 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().withSource( new HiddenBinding()
                {
                } ).bind( Bean.class ).annotatedWith( Names.named( "HIDDEN" ) ).to( BeanImpl.class );
            }
        } );

        child3 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M3" ) ).to( BeanImpl.class );
                bind( Bean.class ).to( BeanImpl2.class );
                bind( Bean.class ).annotatedWith( Names.named( "N3" ) ).to( BeanImpl.class );
            }
        } );

        child4 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                // no bindings
            }
        } );
    }

    public void testDefaultLocator()
    {
        final BeanLocator locator = parent.getInstance( BeanLocator.class );
        assertSame( locator, parent.getInstance( MutableBeanLocator.class ) );

        Iterator<? extends Entry<Named, Bean>> i;

        i = locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) ).iterator();

        assertTrue( i.hasNext() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        i = locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) ).iterator();

        assertTrue( i.hasNext() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
            // expected
        }

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }
    }

    public void testInjectorPublisherEquality()
    {
        final RankingFunction function1 = new DefaultRankingFunction( 1 );
        final RankingFunction function2 = new DefaultRankingFunction( 2 );

        assertTrue( new InjectorPublisher( parent, function1 ).equals( new InjectorPublisher( parent, function2 ) ) );
        assertTrue( new InjectorPublisher( parent, function2 ).equals( new InjectorPublisher( parent, function1 ) ) );

        assertFalse( new InjectorPublisher( child1, function1 ).equals( new InjectorPublisher( child2, function1 ) ) );
        assertFalse( new InjectorPublisher( child2, function2 ).equals( new InjectorPublisher( child1, function2 ) ) );

        assertFalse( new BindingPublisher()
        {
            public <T> void subscribe( final BindingSubscriber<T> subscriber )
            {
            }

            public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
            {
            }
        }.equals( new InjectorPublisher( child1, function1 ) ) );

        assertFalse( new InjectorPublisher( child2, function2 ).equals( new BindingPublisher()
        {
            public <T> void subscribe( final BindingSubscriber<T> subscriber )
            {
            }

            public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
            {
            }
        } ) );

        assertTrue( new InjectorPublisher( parent, function1 ).hashCode() == new InjectorPublisher( parent, function2 ).hashCode() );
        assertTrue( new InjectorPublisher( parent, function2 ).hashCode() == new InjectorPublisher( parent, function1 ).hashCode() );

        assertFalse( new InjectorPublisher( child1, function1 ).hashCode() == new InjectorPublisher( child2, function1 ).hashCode() );
        assertFalse( new InjectorPublisher( child2, function2 ).hashCode() == new InjectorPublisher( child1, function2 ).hashCode() );
    }

    @SuppressWarnings( "deprecation" )
    public void testInjectorOrdering()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Iterable<? extends Entry<Named, Bean>> roles =
            locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) );

        locator.add( parent, 0 );
        locator.add( child1, 1 );
        locator.add( child2, 2 );
        locator.add( child3, 3 );
        locator.add( child4, 4 );

        Iterator<? extends Entry<Named, Bean>> i;

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertEquals( Names.named( "M1" ), i.next().getKey() );
        assertEquals( Names.named( "N1" ), i.next().getKey() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child1 );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.add( child1, 4 );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M1" ), i.next().getKey() );
        assertEquals( Names.named( "N1" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child2 );
        locator.remove( child2 );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M1" ), i.next().getKey() );
        assertEquals( Names.named( "N1" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child3 );
        locator.add( child3, 5 );
        locator.add( child3, 5 );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertEquals( Names.named( "M1" ), i.next().getKey() );
        assertEquals( Names.named( "N1" ), i.next().getKey() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( parent );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertEquals( Names.named( "M1" ), i.next().getKey() );
        assertEquals( Names.named( "N1" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child1 );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child3 );

        i = roles.iterator();
        assertFalse( i.hasNext() );

        locator.add( parent, 3 );
        locator.add( child1, 2 );
        locator.add( child2, 1 );
        locator.add( child3, 0 );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertEquals( Names.named( "M1" ), i.next().getKey() );
        assertEquals( Names.named( "N1" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.clear();

        i = roles.iterator();
        assertFalse( i.hasNext() );
    }

    @SuppressWarnings( "deprecation" )
    public void testExistingInjectors()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        locator.add( parent, 0 );
        locator.add( child1, 1 );

        Iterable<? extends Entry<Named, Bean>> roles =
            locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) );

        locator.add( child2, 2 );
        locator.add( child3, 3 );

        Iterator<? extends Entry<Named, Bean>> i;

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertEquals( Names.named( "M1" ), i.next().getKey() );
        assertEquals( Names.named( "N1" ), i.next().getKey() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        i = null;
        roles = null;
        System.gc();

        locator.clear();
    }
}
