/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.inject;

import java.util.Iterator;
import java.util.Map.Entry;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class PriorityTest
    extends TestCase
{
    static interface Bean
    {
    }

    static class DefaultBean
        implements Bean
    {
    }

    @org.eclipse.sisu.Priority( 1000 )
    static class MediumPriorityBean
        implements Bean
    {
    }

    @javax.annotation.Priority( 3000 )
    static class HighPriorityBean
        implements Bean
    {
    }

    static class LowPriorityBean
        implements Bean
    {
    }

    static Injector injector = Guice.createInjector( new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind( Bean.class ).annotatedWith( Names.named( "LO" ) ).to( LowPriorityBean.class );
            bind( Bean.class ).annotatedWith( Names.named( "HI" ) ).to( HighPriorityBean.class );
            bind( Bean.class ).to( DefaultBean.class );
            bind( Bean.class ).annotatedWith( Names.named( "MED" ) ).to( MediumPriorityBean.class );
            binder().withSource( Sources.prioritize( 2000 ) ).bind( Bean.class ).annotatedWith( Names.named( "SRC" ) ).to( DefaultBean.class );
        }
    } );

    public void testPriorityOverride()
    {
        final BeanLocator locator = injector.getInstance( BeanLocator.class );

        Iterator<? extends Entry<Named, Bean>> i;

        i = locator.<Named, Bean> locate( Key.get( Bean.class, Named.class ) ).iterator();

        assertTrue( i.hasNext() );
        assertEquals( Names.named( "HI" ), i.next().getKey() );
        assertEquals( Names.named( "SRC" ), i.next().getKey() );
        assertEquals( Names.named( "MED" ), i.next().getKey() );
        assertEquals( Names.named( "default" ), i.next().getKey() );
        assertEquals( Names.named( "LO" ), i.next().getKey() );
        assertFalse( i.hasNext() );
    }
}
