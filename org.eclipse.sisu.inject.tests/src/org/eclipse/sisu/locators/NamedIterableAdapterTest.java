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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class NamedIterableAdapterTest
    extends TestCase
{
    public void testNamedAdapter()
    {
        final Map<Named, String> original = new LinkedHashMap<Named, String>();

        final Map<String, String> adapter =
            new EntryMapAdapter<String, String>( new NamedIterableAdapter<String>( original.entrySet() ) );

        assertEquals( original, adapter );
        original.put( Names.named( "3" ), "C" );
        assertEquals( original, adapter );
        original.put( Names.named( "1" ), "A" );
        assertEquals( original, adapter );
        original.put( Names.named( "2" ), "B" );
        assertEquals( original, adapter );

        assertEquals( "{3=C, 1=A, 2=B}", adapter.toString() );

        final Iterator<Entry<String, String>> i = adapter.entrySet().iterator();
        assertEquals( "3=C", i.next().toString() );
        assertEquals( "1=A", i.next().toString() );
        assertEquals( "2=B", i.next().toString() );

        original.clear();

        assertEquals( original, adapter );
    }

    private static void assertEquals( final Map<Named, String> named, final Map<String, String> hinted )
    {
        final Iterator<Entry<Named, String>> i = named.entrySet().iterator();
        final Iterator<Entry<String, String>> j = hinted.entrySet().iterator();
        while ( i.hasNext() )
        {
            assertTrue( j.hasNext() );
            final Entry<Named, String> lhs = i.next();
            final Entry<String, String> rhs = j.next();
            assertEquals( lhs.getKey().value(), rhs.getKey() );
            assertEquals( lhs.getValue(), rhs.getValue() );
        }
        assertFalse( j.hasNext() );
    }

    public void testUnsupportedOperations()
    {
        final Map<Named, String> original = new LinkedHashMap<Named, String>();

        final Map<String, String> adapter =
            new EntryMapAdapter<String, String>( new NamedIterableAdapter<String>( original.entrySet() ) );

        original.put( Names.named( "1" ), "A" );

        try
        {
            adapter.entrySet().iterator().remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        try
        {
            adapter.entrySet().iterator().next().setValue( "B" );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }
}
