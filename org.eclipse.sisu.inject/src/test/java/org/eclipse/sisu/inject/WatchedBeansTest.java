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

import javax.inject.Named;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.inject.LocatedBeansTest.Marked;
import org.eclipse.sisu.inject.RankedBindingsTest.Bean;
import org.eclipse.sisu.inject.RankedBindingsTest.BeanImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WatchedBeansTest
{
    Injector parent;

    Injector child1;

    Injector child2;

    Injector child3;

    @BeforeEach
    void setUp()
        throws Exception
    {
        parent = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "A" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "B" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "C" ) ).to( BeanImpl.class );
            }
        } );

        child1 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "X" ) ).to( BeanImpl.class );
            }
        } );

        child2 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "Y" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Marked.class ).to( BeanImpl.class );
            }
        } );

        child3 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "Z" ) ).to( BeanImpl.class );
            }
        } );
    }

    static class RankingMediator
        implements Mediator<Named, Bean, RankedSequence<String>>
    {
        public void add( final BeanEntry<Named, Bean> entry, final RankedSequence<String> names )
        {
            names.insert( entry.getKey().value(), entry.getRank() );
        }

        public void remove( final BeanEntry<Named, Bean> entry, final RankedSequence<String> names )
        {
            assertNotNull( names.remove( entry.getKey().value() ) );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Test
    void testWatchedBeans()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();
        RankedSequence<String> names = new RankedSequence<String>();

        locator.watch( Key.get( Bean.class, Named.class ), new RankingMediator(), names );

        assertTrue( names.isEmpty() );

        publishInjector( locator, parent, 0 );

        checkNames( names, "A", "B", "C" );

        publishInjector( locator, child1, 1 );

        checkNames( names, "X", "A", "B", "C" );

        final BindingSubscriber[] subscriberHolder = new BindingSubscriber[1];
        final BindingPublisher subscriberHook = new BindingPublisher()
        {
            public <T> void subscribe( final BindingSubscriber<T> subscriber )
            {
                subscriberHolder[0] = subscriber;
            }

            public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
            {
                subscriberHolder[0] = null;
            }

            public int maxBindingRank()
            {
                return Integer.MIN_VALUE;
            }

            public <T> T adapt( final Class<T> type )
            {
                return null;
            }
        };

        locator.add( subscriberHook );
        assertNotNull( subscriberHolder[0] );

        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ), Integer.MIN_VALUE );
        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Marked.class ) ), Integer.MIN_VALUE );

        checkNames( names, "X", "A", "B", "C", "Y" );

        unpublishInjector( locator, parent );

        checkNames( names, "X", "Y" );

        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ) );
        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Marked.class ) ) );

        locator.remove( subscriberHook );
        assertNull( subscriberHolder[0] );

        checkNames( names, "X" );

        publishInjector( locator, child1, 42 );

        checkNames( names, "X" );

        locator.remove( subscriberHook );

        checkNames( names, "X" );

        publishInjector( locator, child3, 3 );

        checkNames( names, "Z", "X" );

        publishInjector( locator, parent, 2 );

        checkNames( names, "Z", "A", "B", "C", "X" );

        locator.clear();

        checkNames( names );

        names = null;
        System.gc();

        publishInjector( locator, parent, Integer.MAX_VALUE );
        unpublishInjector( locator, parent );
    }

    static class BrokenMediator
        implements Mediator<Named, Bean, Object>
    {
        public void add( final BeanEntry<Named, Bean> entry, final Object watcher )
        {
            throw new LinkageError();
        }

        public void remove( final BeanEntry<Named, Bean> entry, final Object watcher )
        {
            throw new LinkageError();
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Test
    void testBrokenWatcher()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        Object keepAlive = new Object();

        publishInjector( locator, parent, 0 );
        locator.watch( Key.get( Bean.class, Named.class ), new BrokenMediator(), keepAlive );
        unpublishInjector( locator, parent );

        final BindingSubscriber[] subscriberHolder = new BindingSubscriber[1];
        final BindingPublisher subscriberHook = new BindingPublisher()
        {
            public <T> void subscribe( final BindingSubscriber<T> subscriber )
            {
                subscriberHolder[0] = subscriber;
            }

            public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
            {
                subscriberHolder[0] = null;
            }

            public int maxBindingRank()
            {
                return 0;
            }

            public <T> T adapt( final Class<T> type )
            {
                return null;
            }
        };

        locator.add( subscriberHook );

        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ), Integer.MIN_VALUE );
        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Marked.class ) ), Integer.MIN_VALUE );

        keepAlive = null;
        System.gc();

        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ) );
        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Marked.class ) ) );
    }

    private static void checkNames( final Iterable<String> actual, final String... expected )
    {
        final Iterator<String> itr = actual.iterator();
        for ( final String n : expected )
        {
            assertEquals( n, itr.next() );
        }
        assertFalse( itr.hasNext() );
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
