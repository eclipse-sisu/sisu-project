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

import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.locators.LocatedBeansTest.Marked;
import org.eclipse.sisu.locators.RankedBindingsTest.Bean;
import org.eclipse.sisu.locators.RankedBindingsTest.BeanImpl;
import org.eclipse.sisu.locators.spi.BindingPublisher;
import org.eclipse.sisu.locators.spi.BindingSubscriber;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class WatchedBeansTest
    extends TestCase
{
    Injector parent;

    Injector child1;

    Injector child2;

    Injector child3;

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
            assertTrue( names.remove( entry.getKey().value() ) );
        }
    }

    @SuppressWarnings( { "deprecation", "rawtypes", "unchecked" } )
    public void testWatchedBeans()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();
        RankedSequence<String> names = new RankedSequence<String>();

        locator.watch( Key.get( Bean.class, Named.class ), new RankingMediator(), names );

        assertTrue( names.isEmpty() );

        locator.add( parent, 0 );

        checkNames( names, "A", "B", "C" );

        locator.add( child1, 1 );

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
        };

        locator.add( subscriberHook, Integer.MIN_VALUE );
        assertNotNull( subscriberHolder[0] );

        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ), Integer.MIN_VALUE );
        subscriberHolder[0].add( child2.getBinding( Key.get( Bean.class, Marked.class ) ), Integer.MIN_VALUE );

        checkNames( names, "X", "A", "B", "C", "Y" );

        locator.remove( parent );

        checkNames( names, "X", "Y" );

        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Names.named( "Y" ) ) ) );
        subscriberHolder[0].remove( child2.getBinding( Key.get( Bean.class, Marked.class ) ) );

        locator.remove( subscriberHook );
        assertNull( subscriberHolder[0] );

        checkNames( names, "X" );

        locator.add( child1, 42 );

        checkNames( names, "X" );

        locator.remove( subscriberHook );

        checkNames( names, "X" );

        locator.add( child3, 3 );

        checkNames( names, "Z", "X" );

        locator.add( parent, 2 );

        checkNames( names, "Z", "A", "B", "C", "X" );

        locator.clear();

        checkNames( names );

        names = null;
        System.gc();

        locator.add( parent, Integer.MAX_VALUE );
        locator.remove( parent );
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

    @SuppressWarnings( { "deprecation", "rawtypes", "unchecked" } )
    public void testBrokenWatcher()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        Object keepAlive = new Object();

        locator.add( parent, 0 );
        locator.watch( Key.get( Bean.class, Named.class ), new BrokenMediator(), keepAlive );
        locator.remove( parent );

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
        };

        locator.add( subscriberHook, 0 );

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
}
