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
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.eclipse.sisu.locators.spi.BindingPublisher;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class RankedBindingsTest
    extends TestCase
{
    @ImplementedBy( BeanImpl2.class )
    static interface Bean
    {
    }

    static abstract class AbstractBean
        implements Bean
    {
    }

    static class BeanImpl
        extends AbstractBean
    {
    }

    static class BeanImpl2
        extends AbstractBean
    {
    }

    static Injector injector0 = Guice.createInjector();

    static Injector injector1 = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( Bean.class ).annotatedWith( Names.named( "1" ) ).to( BeanImpl.class );
        }
    } );

    static Injector injector2 = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( Bean.class ).annotatedWith( Names.named( "2" ) ).to( BeanImpl.class );
            bind( Bean.class ).to( BeanImpl.class );
        }
    } );

    static Injector injector3 = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( Bean.class ).annotatedWith( Names.named( "3" ) ).to( BeanImpl.class );
        }
    } );

    public void testExistingExporters()
    {
        final RankedSequence<BindingPublisher> exporters = new RankedSequence<BindingPublisher>();

        RankingFunction function;

        try
        {
            new DefaultRankingFunction( -1 );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( final IllegalArgumentException e )
        {
            // expected
        }

        function = new DefaultRankingFunction( 0 );
        exporters.insert( new InjectorPublisher( injector0, function ), function.maxRank() );
        function = new DefaultRankingFunction( 1 );
        exporters.insert( new InjectorPublisher( injector1, function ), function.maxRank() );
        function = new DefaultRankingFunction( 3 );
        exporters.insert( new InjectorPublisher( injector3, function ), function.maxRank() );
        function = new DefaultRankingFunction( 2 );
        exporters.insert( new InjectorPublisher( injector2, function ), function.maxRank() );

        final RankedBindings<Bean> bindings = new RankedBindings<Bean>( TypeLiteral.get( Bean.class ), exporters );

        final Iterator<Binding<Bean>> itr = bindings.iterator();

        assertEquals( 0, bindings.bindings.size() );
        assertTrue( itr.hasNext() );
        assertEquals( 3, bindings.bindings.size() );

        final Binding<Bean> explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        assertEquals( 3, bindings.bindings.size() );
        assertTrue( itr.hasNext() );
        assertEquals( 4, bindings.bindings.size() );

        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );

        assertFalse( itr.hasNext() );
    }

    public void testPendingExporters()
    {
        final RankedBindings<Bean> bindings = new RankedBindings<Bean>( TypeLiteral.get( Bean.class ), null );

        Iterator<Binding<Bean>> itr = bindings.iterator();

        assertFalse( itr.hasNext() );

        try
        {
            itr.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
            // expected
        }

        try
        {
            itr.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }

        RankingFunction function;

        assertEquals( 0, bindings.bindings.size() );
        function = new DefaultRankingFunction( 2 );
        bindings.add( new InjectorPublisher( injector2, function ), function.maxRank() );
        assertEquals( 0, bindings.bindings.size() );

        assertTrue( itr.hasNext() );

        assertEquals( 2, bindings.bindings.size() );
        Binding<Bean> explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );
        assertEquals( 2, bindings.bindings.size() );

        assertTrue( itr.hasNext() );

        assertEquals( 2, bindings.bindings.size() );
        function = new DefaultRankingFunction( 0 );
        bindings.add( new InjectorPublisher( injector0, function ), function.maxRank() );
        assertEquals( 2, bindings.bindings.size() );
        function = new DefaultRankingFunction( 3 );
        bindings.add( new InjectorPublisher( injector3, function ), function.maxRank() );
        assertEquals( 2, bindings.bindings.size() );

        assertTrue( itr.hasNext() );

        assertEquals( 3, bindings.bindings.size() );

        assertEquals( 3, bindings.bindings.size() );
        function = new DefaultRankingFunction( 1 );
        bindings.add( new InjectorPublisher( injector1, function ), function.maxRank() );
        assertEquals( 3, bindings.bindings.size() );

        assertTrue( itr.hasNext() );

        assertEquals( 4, bindings.bindings.size() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertEquals( 4, bindings.bindings.size() );

        assertTrue( itr.hasNext() );

        assertEquals( 4, bindings.bindings.size() );

        itr = bindings.iterator();

        explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );

        assertFalse( itr.hasNext() );
    }

    public void testExporterRemoval()
    {
        final BindingPublisher exporter0 = new InjectorPublisher( injector0, new DefaultRankingFunction( 0 ) );
        final BindingPublisher exporter1 = new InjectorPublisher( injector1, new DefaultRankingFunction( 1 ) );
        final BindingPublisher exporter2 = new InjectorPublisher( injector2, new DefaultRankingFunction( 2 ) );
        final BindingPublisher exporter3 = new InjectorPublisher( injector3, new DefaultRankingFunction( 3 ) );

        final RankedBindings<Bean> bindings = new RankedBindings<Bean>( TypeLiteral.get( Bean.class ), null );

        bindings.add( exporter0, 0 );
        bindings.add( exporter1, 1 );
        bindings.add( exporter2, 2 );
        bindings.add( exporter3, 3 );

        Iterator<Binding<Bean>> itr = bindings.iterator();

        bindings.remove( exporter1 );
        assertTrue( itr.hasNext() );

        Binding<Bean> explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        bindings.remove( injector3.findBindingsByType( TypeLiteral.get( Bean.class ) ).get( 0 ) );
        bindings.remove( exporter2 );
        bindings.remove( injector1.findBindingsByType( TypeLiteral.get( Bean.class ) ).get( 0 ) );

        assertFalse( itr.hasNext() );

        itr = bindings.iterator();

        bindings.bindings.clear();

        bindings.add( exporter3, 0 );
        bindings.add( exporter1, 0 );
        bindings.add( exporter0, 0 );
        bindings.add( exporter2, 0 );

        assertTrue( itr.hasNext() );

        explicitBinding = itr.next();
        assertNull( explicitBinding.getKey().getAnnotation() );
        assertEquals( BeanImpl.class, explicitBinding.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "3" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "2" ), itr.next().getKey().getAnnotation() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "1" ), itr.next().getKey().getAnnotation() );

        assertFalse( itr.hasNext() );
        assertFalse( itr.hasNext() );
    }
}
