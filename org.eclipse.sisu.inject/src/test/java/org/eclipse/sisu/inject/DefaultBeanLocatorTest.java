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
package org.eclipse.sisu.inject;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.eclipse.sisu.inject.RankedBindingsTest.Bean;
import org.eclipse.sisu.inject.RankedBindingsTest.BeanImpl;
import org.eclipse.sisu.inject.RankedBindingsTest.BeanImpl2;
import org.eclipse.sisu.inject.RankedBindingsTest.InternalBeanImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class DefaultBeanLocatorTest
{
    Injector parent;

    Injector child1;

    Injector child2;

    Injector child3;

    Injector child4;

    @BeforeEach
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
                binder().withSource( Sources.hide() ).bind( Bean.class ).annotatedWith( Names.named( "HIDDEN" ) ).to( BeanImpl.class );
                binder().bind( Bean.class ).annotatedWith( Names.named( "@INTERNAL" ) ).to( InternalBeanImpl.class );
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

    @Test
    void testDefaultLocator()
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

    @Test
    void testInjectorPublisherEquality()
    {
        final RankingFunction function1 = new DefaultRankingFunction( 1 );
        final RankingFunction function2 = new DefaultRankingFunction( 2 );

        assertTrue( new InjectorBindings( parent, function1 ).equals( new InjectorBindings( parent, function2 ) ) );
        assertTrue( new InjectorBindings( parent, function2 ).equals( new InjectorBindings( parent, function1 ) ) );

        assertFalse( new InjectorBindings( child1, function1 ).equals( new InjectorBindings( child2, function1 ) ) );
        assertFalse( new InjectorBindings( child2, function2 ).equals( new InjectorBindings( child1, function2 ) ) );

        assertFalse( new BindingPublisher()
        {
            public <T> void subscribe( final BindingSubscriber<T> subscriber )
            {
            }

            public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
            {
            }

            public int maxBindingRank()
            {
                return 0;
            }

            public <T> T adapt( final Class<T> type )
            {
                return null;
            }
        }.equals( new InjectorBindings( child1, function1 ) ) );

        assertFalse( new InjectorBindings( child2, function2 ).equals( new BindingPublisher()
        {
            public <T> void subscribe( final BindingSubscriber<T> subscriber )
            {
            }

            public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
            {
            }

            public int maxBindingRank()
            {
                return 0;
            }

            public <T> T adapt( final Class<T> type )
            {
                return null;
            }
        } ) );

        assertTrue( new InjectorBindings( parent,
                                          function1 ).hashCode() == new InjectorBindings( parent,
                                                                                          function2 ).hashCode() );
        assertTrue( new InjectorBindings( parent,
                                          function2 ).hashCode() == new InjectorBindings( parent,
                                                                                          function1 ).hashCode() );

        assertFalse( new InjectorBindings( child1,
                                           function1 ).hashCode() == new InjectorBindings( child2,
                                                                                           function1 ).hashCode() );
        assertFalse( new InjectorBindings( child2,
                                           function2 ).hashCode() == new InjectorBindings( child1,
                                                                                           function2 ).hashCode() );
    }

    @Test
    void testInjectorOrdering()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Iterable<? extends Entry<Named, Bean>> roles =
            locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) );

        publishInjector( locator, parent, 0 );
        publishInjector( locator, child1, 1 );
        publishInjector( locator, child2, 2 );
        publishInjector( locator, child3, 3 );
        publishInjector( locator, child4, 4 );

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

        unpublishInjector( locator, child1 );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertEquals( Names.named( "A" ), i.next().getKey() );
        assertEquals( Names.named( "-" ), i.next().getKey() );
        assertEquals( Names.named( "Z" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        publishInjector( locator, child1, 4 );

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

        unpublishInjector( locator, child2 );
        unpublishInjector( locator, child2 );

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

        unpublishInjector( locator, child3 );
        publishInjector( locator, child3, 5 );
        publishInjector( locator, child3, 5 );

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

        unpublishInjector( locator, parent );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertEquals( Names.named( "M1" ), i.next().getKey() );
        assertEquals( Names.named( "N1" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        unpublishInjector( locator, child1 );

        i = roles.iterator();
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "M3" ), i.next().getKey() );
        assertEquals( Names.named( "N3" ), i.next().getKey() );
        assertFalse( i.hasNext() );

        unpublishInjector( locator, child3 );

        i = roles.iterator();
        assertFalse( i.hasNext() );

        publishInjector( locator, parent, 3 );
        publishInjector( locator, child1, 2 );
        publishInjector( locator, child2, 1 );
        publishInjector( locator, child3, 0 );

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

    @Test
    void testExistingInjectors()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        publishInjector( locator, parent, 0 );
        publishInjector( locator, child1, 1 );

        Iterable<? extends Entry<Named, Bean>> roles =
            locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) );

        publishInjector( locator, child2, 2 );
        publishInjector( locator, child3, 3 );

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

    private static void publishInjector( final MutableBeanLocator locator, final Injector injector, final int rank )
    {
        locator.add( new InjectorBindings( injector, new DefaultRankingFunction( rank ) ) );
    }

    private static void unpublishInjector( final MutableBeanLocator locator, final Injector injector )
    {
        locator.remove( new InjectorBindings( injector, null /* unused */ ) );
    }
}
