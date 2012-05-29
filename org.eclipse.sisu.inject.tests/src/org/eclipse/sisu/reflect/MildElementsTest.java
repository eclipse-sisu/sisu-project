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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class MildElementsTest
    extends TestCase
{
    public void testSoftElements()
    {
        testElements( true );
    }

    public void testWeakElements()
    {
        testElements( false );
    }

    private static void testElements( final boolean soft )
    {
        final Collection<String> names = new MildElements<String>( new ArrayList<Reference<String>>(), soft );

        String a = new String( "A" ), b = new String( "B" ), c = new String( "C" );

        assertTrue( names.isEmpty() );
        assertEquals( 0, names.size() );

        names.add( a );

        assertFalse( names.isEmpty() );
        assertEquals( 1, names.size() );

        names.add( b );

        assertFalse( names.isEmpty() );
        assertEquals( 2, names.size() );

        names.add( c );

        assertFalse( names.isEmpty() );
        assertEquals( 3, names.size() );

        Iterator<String> itr = names.iterator();

        assertTrue( itr.hasNext() );
        assertEquals( "A", itr.next() );
        assertEquals( "B", itr.next() );
        itr.remove();
        assertTrue( itr.hasNext() );
        assertEquals( "C", itr.next() );
        assertFalse( itr.hasNext() );

        names.add( b = new String( "b2b" ) );

        itr = names.iterator();

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

        try
        {
            itr.remove();
            fail( "Expected IllegalStateException" );
        }
        catch ( final IllegalStateException e )
        {
        }

        itr = null;
        int size;

        size = names.size();
        c = null; // clear so element can be evicted
        gc( names, size );

        itr = names.iterator();

        assertEquals( "A", itr.next() );
        assertEquals( "b2b", itr.next() );
        assertFalse( itr.hasNext() );
        itr = null;

        size = names.size();
        a = null; // clear so element can be evicted
        gc( names, size );

        itr = names.iterator();

        assertEquals( "b2b", itr.next() );
        assertFalse( itr.hasNext() );
        itr = null;

        size = names.size();
        b = null; // clear so element can be evicted
        gc( names, size );

        itr = names.iterator();

        assertFalse( itr.hasNext() );
    }

    private static int gc( final Collection<?> elements, final int size )
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
        while ( elements.size() == size && gcCount < 1024 );

        return hash;
    }
}
