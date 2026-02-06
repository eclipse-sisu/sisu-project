/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu.inject;

import java.util.Arrays;
import java.util.Iterator;

import javax.inject.Named;

import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings( { "deprecation", "rawtypes", "unchecked" } )
@BaseTests
class LegacyTest
{
    static class TestBeanEntry
        implements org.eclipse.sisu.BeanEntry<Named, String>
    {
        private final String value;

        TestBeanEntry( final String value )
        {
            this.value = value;
        }

        public Named getKey()
        {
            return null;
        }

        public String getValue()
        {
            return value;
        }

        public String setValue( final String newValue )
        {
            return value;
        }

        public javax.inject.Provider<String> getProvider()
        {
            return new javax.inject.Provider<String>()
            {
                public String get()
                {
                    return value;
                }
            };
        }

        public String getDescription()
        {
            return "test";
        }

        public Object getSource()
        {
            return null;
        }

        public int getRank()
        {
            return 0;
        }

        public Class<String> getImplementationClass()
        {
            return String.class;
        }
    }

    @Test
    void testAdaptBeanEntry()
    {
        final org.eclipse.sisu.BeanEntry<Named, String> entry = new TestBeanEntry( "hello" );
        final org.sonatype.inject.BeanEntry<Named, String> adapted = Legacy.adapt( entry );
        assertNotNull( adapted );
        assertEquals( "hello", adapted.getValue() );
        assertEquals( String.class, adapted.getImplementationClass() );
        assertEquals( "test", adapted.getDescription() );
    }

    @Test
    void testAdaptNullBeanEntry()
    {
        final org.sonatype.inject.BeanEntry adapted = Legacy.adapt( (org.eclipse.sisu.BeanEntry) null );
        assertNull( adapted );
    }

    @Test
    void testAdaptIterable()
    {
        final org.eclipse.sisu.BeanEntry<Named, String> entryA = new TestBeanEntry( "a" );
        final org.eclipse.sisu.BeanEntry<Named, String> entryB = new TestBeanEntry( "b" );

        final Iterable<? extends org.eclipse.sisu.BeanEntry<Named, String>> delegate = Arrays.asList( entryA, entryB );
        final Iterable<org.sonatype.inject.BeanEntry<Named, String>> adapted = Legacy.adapt( delegate );

        final Iterator<org.sonatype.inject.BeanEntry<Named, String>> iterator = adapted.iterator();
        assertTrue( iterator.hasNext() );
        assertEquals( "a", iterator.next().getValue() );
        assertTrue( iterator.hasNext() );
        assertEquals( "b", iterator.next().getValue() );
        assertFalse( iterator.hasNext() );
    }

    @Test
    void testAdaptProvider()
    {
        final org.eclipse.sisu.BeanEntry<Named, String> entry = new TestBeanEntry( "provided" );
        final Iterable<? extends org.eclipse.sisu.BeanEntry<Named, String>> entries = Arrays.asList( entry );

        final com.google.inject.Provider<Iterable<? extends org.eclipse.sisu.BeanEntry<Named, String>>> delegate =
            new com.google.inject.Provider<Iterable<? extends org.eclipse.sisu.BeanEntry<Named, String>>>()
            {
                public Iterable<? extends org.eclipse.sisu.BeanEntry<Named, String>> get()
                {
                    return entries;
                }
            };

        final com.google.inject.Provider<Iterable<org.sonatype.inject.BeanEntry<Named, String>>> adapted =
            Legacy.adapt( delegate );

        final Iterator<org.sonatype.inject.BeanEntry<Named, String>> iterator = adapted.get().iterator();
        assertTrue( iterator.hasNext() );
        assertEquals( "provided", iterator.next().getValue() );
    }

    @Test
    void testAdaptNullMediator()
    {
        final org.eclipse.sisu.Mediator adapted = Legacy.adapt( (org.sonatype.inject.Mediator) null );
        assertNull( adapted );
    }

    @Test
    void testAdaptMediator()
        throws Exception
    {
        final org.sonatype.inject.Mediator<Named, String, Object> delegate =
            new org.sonatype.inject.Mediator<Named, String, Object>()
            {
                public void add( final org.sonatype.inject.BeanEntry<Named, String> entry, final Object watcher )
                {
                    // test add
                }

                public void remove( final org.sonatype.inject.BeanEntry<Named, String> entry, final Object watcher )
                {
                    // test remove
                }
            };
        final org.eclipse.sisu.Mediator<Named, String, Object> adapted = Legacy.adapt( delegate );
        assertNotNull( adapted );
        adapted.add( new TestBeanEntry( "x" ), new Object() );
        adapted.remove( new TestBeanEntry( "y" ), new Object() );
    }

    @Test
    void testLegacyAsFactory()
    {
        final Legacy<Runnable> legacy = Legacy.as( Runnable.class );
        assertNotNull( legacy );
        final Runnable delegate = new Runnable()
        {
            public void run()
            {
                // nothing
            }
        };
        final Runnable proxy = legacy.proxy( delegate );
        assertNotNull( proxy );
        proxy.run(); // should delegate
    }

    @Test
    void testLegacyProxyNull()
    {
        final Legacy<Runnable> legacy = Legacy.as( Runnable.class );
        assertNull( legacy.proxy( null ) );
    }
}
