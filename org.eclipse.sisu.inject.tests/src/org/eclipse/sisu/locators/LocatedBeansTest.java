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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.util.Iterator;

import javax.inject.Named;
import javax.inject.Qualifier;

import junit.framework.TestCase;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.locators.RankedBindingsTest.Bean;
import org.eclipse.sisu.locators.RankedBindingsTest.BeanImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

public class LocatedBeansTest
    extends TestCase
{
    @Qualifier
    @Retention( RUNTIME )
    public @interface Marked
    {
        String value() default "";
    }

    @Marked( "MarkedBean1" )
    static class MarkedBeanImpl1
        implements Bean
    {
    }

    @Marked( "MarkedBean2" )
    static class MarkedBeanImpl2
        implements Bean
    {
    }

    Injector injector;

    @Override
    public void setUp()
        throws Exception
    {
        injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).to( BeanImpl.class ); // also seen as @Named("default")

                bind( Bean.class ).annotatedWith( Named.class ).to( BeanImpl.class ); // only in unrestricted search

                bind( Bean.class ).annotatedWith( Names.named( "Named1" ) ).toProvider( Providers.of( new BeanImpl() ) );
                bind( Bean.class ).annotatedWith( Names.named( "Named2" ) ).to( BeanImpl.class );

                bind( Bean.class ).annotatedWith( Marked.class ).to( BeanImpl.class ); // only in unrestricted search

                bind( Bean.class ).annotatedWith( MarkedBeanImpl1.class.getAnnotation( Marked.class ) ).to( MarkedBeanImpl1.class );
                bind( Bean.class ).annotatedWith( Names.named( "Marked2" ) ).to( MarkedBeanImpl2.class );
            }
        } );
    }

    public void testCacheConcurrency()
    {
        final LocatedBeans<Annotation, Bean> beans = locate( Key.get( Bean.class ) );

        final Iterator<BeanEntry<Annotation, Bean>> itr1 = beans.iterator();
        final Iterator<BeanEntry<Annotation, Bean>> itr2 = beans.iterator();

        Bean a, b;

        a = itr1.next().getValue();
        assertSame( a, itr2.next().getValue() );
        a = itr1.next().getValue();
        assertSame( a, itr2.next().getValue() );
        a = itr1.next().getValue();
        assertSame( a, itr2.next().getValue() );

        a = itr1.next().getValue();

        for ( final Binding<Bean> binding : beans.beans.bindings() )
        {
            beans.beans.remove( binding );
        }

        b = itr2.next().getValue();

        assertFalse( a == b );

        a = itr1.next().getValue();
        assertSame( a, itr2.next().getValue() );
        a = itr1.next().getValue();
        assertSame( a, itr2.next().getValue() );
        a = itr1.next().getValue();
        assertSame( a, itr2.next().getValue() );
    }

    public void testUnrestrictedSearch()
    {
        final LocatedBeans<Annotation, Bean> beans = locate( Key.get( Bean.class ) );
        final Iterator<BeanEntry<Annotation, Bean>> itr = beans.iterator();

        assertTrue( itr.hasNext() );
        assertEquals( QualifyingStrategy.DEFAULT_QUALIFIER, itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( QualifyingStrategy.BLANK_QUALIFIER, itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "Named1" ), itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "Named2" ), itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( QualifyingStrategy.BLANK_QUALIFIER, itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( MarkedBeanImpl1.class.getAnnotation( Marked.class ), itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "Marked2" ), itr.next().getKey() );
        assertFalse( itr.hasNext() );
    }

    public void testNamedSearch()
    {
        final LocatedBeans<Named, Bean> beans = locate( Key.get( Bean.class, Named.class ) );
        final Iterator<BeanEntry<Named, Bean>> itr = beans.iterator();

        assertTrue( itr.hasNext() );
        assertEquals( QualifyingStrategy.DEFAULT_QUALIFIER, itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "Named1" ), itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "Named2" ), itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "Marked2" ), itr.next().getKey() );
        assertFalse( itr.hasNext() );
    }

    public void testNamedWithAttributesSearch()
    {
        final LocatedBeans<Named, Bean> beans = locate( Key.get( Bean.class, Names.named( "Named2" ) ) );
        final Iterator<BeanEntry<Named, Bean>> itr = beans.iterator();

        assertTrue( itr.hasNext() );
        assertEquals( Names.named( "Named2" ), itr.next().getKey() );
        assertFalse( itr.hasNext() );
    }

    public void testMarkedSearch()
    {
        final LocatedBeans<Marked, Bean> beans = locate( Key.get( Bean.class, Marked.class ) );
        final Iterator<BeanEntry<Marked, Bean>> itr = beans.iterator();

        assertTrue( itr.hasNext() );
        assertEquals( MarkedBeanImpl1.class.getAnnotation( Marked.class ), itr.next().getKey() );
        assertTrue( itr.hasNext() );
        assertEquals( MarkedBeanImpl2.class.getAnnotation( Marked.class ), itr.next().getKey() );
        assertFalse( itr.hasNext() );
    }

    public void testMarkedWithAttributesSearch()
    {
        final LocatedBeans<Marked, Bean> beans =
            locate( Key.get( Bean.class, MarkedBeanImpl2.class.getAnnotation( Marked.class ) ) );
        final Iterator<BeanEntry<Marked, Bean>> itr = beans.iterator();

        assertTrue( itr.hasNext() );
        assertEquals( MarkedBeanImpl2.class.getAnnotation( Marked.class ), itr.next().getKey() );
        assertFalse( itr.hasNext() );
    }

    private <Q extends Annotation, T> LocatedBeans<Q, T> locate( final Key<T> key )
    {
        final RankedBindings<T> bindings = new RankedBindings<T>( key.getTypeLiteral(), null );
        for ( final Binding<T> b : injector.findBindingsByType( key.getTypeLiteral() ) )
        {
            bindings.add( b, 0 );
        }
        return new LocatedBeans<Q, T>( key, bindings, null );
    }
}
