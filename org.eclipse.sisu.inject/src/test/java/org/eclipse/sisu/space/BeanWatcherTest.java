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
package org.eclipse.sisu.space;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.EagerSingleton;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.inject.InjectorBindings;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeanWatcherTest
{
    @Qualifier
    @Retention( RetentionPolicy.RUNTIME )
    public @interface Marked
    {
        int value();
    }

    static abstract class Item
    {
    }

    static abstract class SomeItem
        extends Item
    {
    }

    @javax.inject.Named
    static class AItem
        extends SomeItem
    {
    }

    @Marked( 0 )
    @javax.inject.Named
    static class BItem
        extends SomeItem
    {
    }

    @EagerSingleton
    @javax.inject.Named
    static class CItem
        extends SomeItem
    {
        static boolean initialized;

        public CItem()
        {
            initialized = true;
        }
    }

    @Marked( 1 )
    static class DItem
        extends SomeItem
    {
    }

    static class EItem
        extends SomeItem
    {
    }

    @javax.inject.Named
    static class NamedItemWatcher
    {
        Map<String, Item> items = new HashMap<String, Item>();
    }

    @javax.inject.Named
    static class MarkedItemWatcher
    {
        Map<Integer, Item> items = new HashMap<Integer, Item>();
    }

    @javax.inject.Named
    static class AnnotatedItemWatcher
    {
        Map<Annotation, Item> items = new HashMap<Annotation, Item>();
    }

    @javax.inject.Named
    static class NamedItemMediator
        implements Mediator<javax.inject.Named, Item, NamedItemWatcher>
    {
        public void add( final BeanEntry<javax.inject.Named, Item> bean, final NamedItemWatcher watcher )
            throws Exception
        {
            assertNull( watcher.items.put( bean.getKey().value(), bean.getValue() ) );
        }

        public void remove( final BeanEntry<javax.inject.Named, Item> bean, final NamedItemWatcher watcher )
            throws Exception
        {
            assertEquals( watcher.items.remove( bean.getKey().value() ), bean.getValue() );
        }
    }

    @javax.inject.Named
    static class MarkedItemMediator
        implements Mediator<Marked, Item, MarkedItemWatcher>
    {
        public void add( final BeanEntry<Marked, Item> bean, final MarkedItemWatcher watcher )
            throws Exception
        {
            assertNull( watcher.items.put( Integer.valueOf( bean.getKey().value() ), bean.getValue() ) );
        }

        public void remove( final BeanEntry<Marked, Item> bean, final MarkedItemWatcher watcher )
            throws Exception
        {
            assertEquals( watcher.items.remove( Integer.valueOf( bean.getKey().value() ) ), bean.getValue() );
        }
    }

    @javax.inject.Named
    static class AnnotatedItemMediator
        implements Mediator<Annotation, Item, AnnotatedItemWatcher>
    {
        public void add( final BeanEntry<Annotation, Item> bean, final AnnotatedItemWatcher watcher )
            throws Exception
        {
            assertNull( watcher.items.put( bean.getKey(), bean.getValue() ) );
        }

        public void remove( final BeanEntry<Annotation, Item> bean, final AnnotatedItemWatcher watcher )
            throws Exception
        {
            assertEquals( watcher.items.remove( bean.getKey() ), bean.getValue() );
        }
    }

    @Inject
    private NamedItemWatcher namedItemWatcher;

    @Inject
    private MarkedItemWatcher markedItemWatcher;

    @Inject
    private AnnotatedItemWatcher annotatedItemWatcher;

    @Inject
    private Injector injector;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        final ClassSpace space =
            new URLClassSpace( getClass().getClassLoader(), new URL[] { getClass().getResource( "" ) } );

        Guice.createInjector( new SpaceModule( space ), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Item.class ).annotatedWith( new QualifiedTypesTest.LegacyImpl() ).to( EItem.class );
            }
        } ).injectMembers( this );
    }

    @Test
    public void testWatchers()
    {
        assertTrue( CItem.initialized );

        assertEquals( 4, namedItemWatcher.items.size() );
        assertEquals( 2, markedItemWatcher.items.size() );
        assertEquals( 5, annotatedItemWatcher.items.size() );

        assertTrue( namedItemWatcher.items.get( AItem.class.getName() ) instanceof AItem );
        assertTrue( namedItemWatcher.items.get( BItem.class.getName() ) instanceof BItem );
        assertTrue( namedItemWatcher.items.get( CItem.class.getName() ) instanceof CItem );
        assertTrue( namedItemWatcher.items.get( DItem.class.getName() ) instanceof DItem );

        assertNotSame( namedItemWatcher.items.get( AItem.class.getName() ), injector.getInstance( AItem.class ) );
        assertSame( namedItemWatcher.items.get( CItem.class.getName() ), injector.getInstance( CItem.class ) );

        assertTrue( markedItemWatcher.items.get( Integer.valueOf( 0 ) ) instanceof BItem );
        assertTrue( markedItemWatcher.items.get( Integer.valueOf( 1 ) ) instanceof DItem );

        injector.getInstance( MutableBeanLocator.class ).remove( new InjectorBindings( injector, null /* unused */ ) );

        assertEquals( 0, namedItemWatcher.items.size() );
        assertEquals( 0, markedItemWatcher.items.size() );
        assertEquals( 0, annotatedItemWatcher.items.size() );
    }
}
