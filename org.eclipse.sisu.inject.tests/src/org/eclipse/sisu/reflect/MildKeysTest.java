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
package org.eclipse.sisu.reflect;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class MildKeysTest
    extends TestCase
{
    public void testSoftKeys()
    {
        testKeys( true );
    }

    public void testWeakKeys()
    {
        testKeys( false );
    }

    private static void testKeys( final boolean soft )
    {
        final Map<String, String> names =
            new MildKeys<String, String>( new LinkedHashMap<Reference<String>, String>(), soft );

        String a = new String( "A" ), b = new String( "B" ), c = new String( "C" );

        assertTrue( names.isEmpty() );
        assertEquals( 0, names.size() );

        names.put( a, "1" );

        assertFalse( names.isEmpty() );
        assertEquals( 1, names.size() );

        names.put( b, "2" );

        assertFalse( names.isEmpty() );
        assertEquals( 2, names.size() );

        names.put( c, "3" );

        assertFalse( names.isEmpty() );
        assertEquals( 3, names.size() );

        Iterator<String> itr = names.values().iterator();

        assertTrue( itr.hasNext() );
        assertEquals( "1", itr.next() );
        assertEquals( "2", itr.next() );
        itr.remove();
        assertTrue( itr.hasNext() );
        assertEquals( "3", itr.next() );
        assertFalse( itr.hasNext() );

        names.put( b = new String( "b2b" ), "2" );

        itr = names.keySet().iterator();

        assertEquals( "A", itr.next() );
        assertEquals( "C", itr.next() );
        assertEquals( "b2b", itr.next() );

        try
        {
            itr.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }

        itr = null;
        int size;

        size = names.size();
        c = null; // clear so element can be evicted
        gc( names, size );

        itr = names.keySet().iterator();

        assertEquals( "A", itr.next() );
        assertEquals( "b2b", itr.next() );
        assertFalse( itr.hasNext() );
        itr = null;

        size = names.size();
        a = null; // clear so element can be evicted
        gc( names, size );

        itr = names.keySet().iterator();

        assertEquals( "b2b", itr.next() );
        assertFalse( itr.hasNext() );
        itr = null;

        size = names.size();
        b = null; // clear so element can be evicted
        gc( names, size );

        itr = names.keySet().iterator();

        assertFalse( itr.hasNext() );
    }

    private static int gc( final Map<?, ?> map, final int size )
    {
        /*
         * Keep forcing GC until the collection compacts itself
         */
        int gcCount = 0, hash = 0;
        do
        {
            try
            {
                final List<byte[]> buf = new LinkedList<byte[]>();
                for ( int i = 0; i < 1024 * 1024; i++ )
                {
                    // try to trigger aggressive GC
                    buf.add( new byte[1024 * 1024] );
                }
                hash += buf.hashCode(); // so JIT doesn't optimize this away
            }
            catch ( final OutOfMemoryError e )
            {
                // ignore...
            }

            System.gc();
            gcCount++;
        }
        while ( map.size() == size && gcCount < 1024 );

        return hash;
    }
}
