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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class EntryListAdapterTest
    extends TestCase
{
    @SuppressWarnings( "boxing" )
    public void testListSize()
    {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        final List<Integer> list = new EntryListAdapter<String, Integer>( map.entrySet() );

        assertTrue( list.isEmpty() );
        map.put( "A", 1 );
        assertFalse( list.isEmpty() );

        assertEquals( 1, list.size() );
        map.put( "C", 3 );
        assertEquals( 2, list.size() );
        map.put( "B", 2 );
        assertEquals( 3, list.size() );
        map.remove( "C" );
        assertEquals( 2, list.size() );
    }

    public void testEmptyList()
    {
        final ListIterator<String> i =
            new EntryListAdapter<Integer, String>( Collections.<Entry<Integer, String>> emptyList() ).listIterator( 0 );

        assertFalse( i.hasNext() );
        assertEquals( 0, i.nextIndex() );
        assertFalse( i.hasPrevious() );
        assertEquals( -1, i.previousIndex() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }

        try
        {
            i.previous();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }

        assertFalse( i.hasNext() );
        assertEquals( 0, i.nextIndex() );
        assertFalse( i.hasPrevious() );
        assertEquals( -1, i.previousIndex() );
    }

    @SuppressWarnings( "boxing" )
    public void testInvalidListIterator()
    {
        final Map<Integer, String> map = new HashMap<Integer, String>();

        map.put( 3, "C" );
        map.put( 1, "A" );
        map.put( 2, "B" );

        try
        {
            new EntryListAdapter<Integer, String>( map.entrySet() ).listIterator( -1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }

        try
        {
            new EntryListAdapter<Integer, String>( map.entrySet() ).listIterator( 4 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }
    }

    @SuppressWarnings( "boxing" )
    public void testIterator()
    {
        final Map<Integer, String> map = new LinkedHashMap<Integer, String>();

        map.put( 3, "C" );
        map.put( 1, "A" );
        map.put( 2, "B" );

        final Iterator<String> i = new EntryListAdapter<Integer, String>( map.entrySet() ).iterator();

        assertTrue( i.hasNext() );

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        assertEquals( "C", i.next() );
        assertTrue( i.hasNext() );

        assertEquals( "A", i.next() );
        assertTrue( i.hasNext() );

        assertEquals( "B", i.next() );
        assertFalse( i.hasNext() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    @SuppressWarnings( "boxing" )
    public void testListIterator()
    {
        final Map<Integer, String> map = new LinkedHashMap<Integer, String>();

        map.put( 3, "C" );
        map.put( 1, "A" );
        map.put( 2, "B" );

        final ListIterator<String> i = new EntryListAdapter<Integer, String>( map.entrySet() ).listIterator( 1 );

        assertTrue( i.hasNext() );
        assertEquals( 1, i.nextIndex() );
        assertTrue( i.hasPrevious() );
        assertEquals( 0, i.previousIndex() );

        try
        {
            i.add( "X" );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        try
        {
            i.set( "X" );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        assertEquals( "A", i.next() );
        assertTrue( i.hasNext() );
        assertEquals( 2, i.nextIndex() );
        assertTrue( i.hasPrevious() );
        assertEquals( 1, i.previousIndex() );

        assertEquals( "B", i.next() );
        assertFalse( i.hasNext() );
        assertEquals( 3, i.nextIndex() );
        assertTrue( i.hasPrevious() );
        assertEquals( 2, i.previousIndex() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }

        assertEquals( "B", i.previous() );
        assertTrue( i.hasNext() );
        assertEquals( 2, i.nextIndex() );
        assertTrue( i.hasPrevious() );
        assertEquals( 1, i.previousIndex() );

        assertEquals( "A", i.previous() );
        assertTrue( i.hasNext() );
        assertEquals( 1, i.nextIndex() );
        assertTrue( i.hasPrevious() );
        assertEquals( 0, i.previousIndex() );

        assertEquals( "C", i.previous() );
        assertTrue( i.hasNext() );
        assertEquals( 0, i.nextIndex() );
        assertFalse( i.hasPrevious() );
        assertEquals( -1, i.previousIndex() );

        try
        {
            i.previous();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }

        assertEquals( "C", i.next() );
        assertTrue( i.hasNext() );
        assertEquals( 1, i.nextIndex() );
        assertTrue( i.hasPrevious() );
        assertEquals( 0, i.previousIndex() );
    }

    @SuppressWarnings( "boxing" )
    public void testVanishingList()
    {
        final Map<Integer, String> map = new LinkedHashMap<Integer, String>();

        map.put( 3, "C" );
        map.put( 1, "A" );
        map.put( 2, "B" );

        final IndexList<Entry<Integer, String>> entries = new IndexList<Entry<Integer, String>>();

        entries.addAll( map.entrySet() );
        final ListIterator<String> i = new EntryListAdapter<Integer, String>( entries ).listIterator( 1 );
        entries.clear();

        assertFalse( i.hasNext() );
        assertEquals( 1, i.nextIndex() );
        assertTrue( i.hasPrevious() );
        assertEquals( 0, i.previousIndex() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }

        assertEquals( "C", i.previous() );
        assertTrue( i.hasNext() );
        assertEquals( 0, i.nextIndex() );
        assertFalse( i.hasPrevious() );
        assertEquals( -1, i.previousIndex() );

        try
        {
            i.previous();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }

        assertEquals( "C", i.next() );
        assertFalse( i.hasNext() );
        assertEquals( 1, i.nextIndex() );
        assertTrue( i.hasPrevious() );
        assertEquals( 0, i.previousIndex() );
    }

    @SuppressWarnings( "boxing" )
    public void testCachedList()
    {
        final Map<Integer, String> map = new LinkedHashMap<Integer, String>();

        map.put( 3, "C" );
        map.put( 1, "A" );
        map.put( 2, "B" );

        final IndexList<Entry<Integer, String>> entries = new IndexList<Entry<Integer, String>>();

        entries.addAll( map.entrySet() );
        final ListIterator<String> i = new EntryListAdapter<Integer, String>( entries ).listIterator( 3 );
        entries.clear();

        assertFalse( i.hasNext() );
        assertEquals( "B", i.previous() );
        assertEquals( "A", i.previous() );
        assertEquals( "C", i.previous() );
        assertFalse( i.hasPrevious() );
        assertEquals( "C", i.next() );
        assertEquals( "A", i.next() );
        assertEquals( "B", i.next() );
        assertFalse( i.hasNext() );
    }

    static final class IndexList<T>
        extends ArrayList<T>
    {
        private static final long serialVersionUID = 1L;

        @Override
        public Iterator<T> iterator()
        {
            return new Iterator<T>()
            {
                int index;

                public boolean hasNext()
                {
                    return index < size();
                }

                public T next()
                {
                    if ( hasNext() )
                    {
                        return get( index++ );
                    }
                    throw new NoSuchElementException();
                }

                public void remove()
                {
                    IndexList.this.remove( index );
                }
            };
        }
    }
}
