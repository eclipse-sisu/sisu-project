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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

public class RankedSequenceTest
    extends TestCase
{
    static final AtomicBoolean active = new AtomicBoolean( true );

    static final List<Throwable> errors = Collections.synchronizedList( new ArrayList<Throwable>() );

    static final Random random = new Random( System.currentTimeMillis() );

    static final int CONCURRENCY = 8;

    static final RankedSequence<Integer> rankedList = new RankedSequence<Integer>();

    public void testOrdering()
    {
        final RankedSequence<String> list = new RankedSequence<String>();

        list.insert( "A1", Integer.MAX_VALUE );
        list.insert( "F", Integer.MIN_VALUE + 1 );
        list.insert( "E", -1 );
        list.insert( "G1", Integer.MIN_VALUE );
        list.insert( "D", 0 );
        list.insert( "B", Integer.MAX_VALUE - 1 );
        list.insert( "C", 1 );
        list.insert( "G2", Integer.MIN_VALUE );
        list.insert( "D", 0 );
        list.insert( "A2", Integer.MAX_VALUE );

        assertEquals( 10, list.size() );

        Iterator<String> itr = list.iterator();

        assertTrue( itr.hasNext() );
        assertTrue( itr.hasNext() );

        assertTrue( itr.hasNext() );
        assertEquals( "A1", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "A2", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "B", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "C", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "D", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "D", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "E", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "F", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "G1", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "G2", itr.next() );
        assertFalse( itr.hasNext() );

        assertFalse( itr.hasNext() );
        assertFalse( itr.hasNext() );

        itr = list.iterator();

        assertEquals( "A1", itr.next() );
        assertEquals( "A2", itr.next() );
        assertEquals( "B", itr.next() );
        assertEquals( "C", itr.next() );
        assertEquals( "D", itr.next() );
        assertEquals( "D", itr.next() );
        assertEquals( "E", itr.next() );
        assertEquals( "F", itr.next() );
        assertEquals( "G1", itr.next() );
        assertEquals( "G2", itr.next() );

        itr = list.iterator();

        list.remove( "D" );
        list.insert( "D-", 0 );
        list.remove( "D" );
        assertEquals( "A1", itr.next() );
        assertEquals( "A2", itr.next() );
        assertTrue( itr.hasNext() );
        list.remove( "B" );
        list.insert( "512", 512 );
        assertEquals( "B", itr.next() );
        assertTrue( itr.hasNext() );
        list.insert( "1024", 1024 );
        assertEquals( "512", itr.next() );
        assertEquals( "C", itr.next() );
        assertEquals( "D-", itr.next() );
        assertEquals( "E", itr.next() );
        assertEquals( "F", itr.next() );
        assertEquals( "G1", itr.next() );
        assertEquals( "G2", itr.next() );
    }

    public void testEmptyList()
    {
        final Iterator<Object> itr = new RankedSequence<Object>().iterator();

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
    }

    public void testConcurrentIteration()
    {
        final Thread[] threads = new Thread[3 * CONCURRENCY];

        int i = 0;
        while ( i < threads.length )
        {
            threads[i++] = new Thread( new Push() );
            threads[i++] = new Thread( new Pull() );
            threads[i++] = new Thread( new Read() );
        }

        for ( final Thread t : threads )
        {
            t.start();
        }

        try
        {
            Thread.sleep( 10 * 1000 );
        }
        catch ( final InterruptedException e )
        {
            // ignore
        }

        active.set( false );

        for ( final Thread t : threads )
        {
            try
            {
                t.join();
            }
            catch ( final InterruptedException e )
            {
                // ignore
            }
        }

        for ( final Throwable e : errors )
        {
            e.printStackTrace();
        }
        if ( errors.size() > 0 )
        {
            fail( "Unexpected errors!" );
        }
    }

    static class Push
        implements Runnable
    {
        public void run()
        {
            while ( active.get() )
            {
                Thread.yield();
                if ( rankedList.size() < 64 * 1024 )
                {
                    final int rank = random.nextInt();
                    rankedList.insert( Integer.valueOf( rank ), rank );
                }
                Thread.yield();
            }
        }
    }

    static class Pull
        implements Runnable
    {
        public void run()
        {
            try
            {
                while ( active.get() || !rankedList.isEmpty() )
                {
                    Thread.yield();
                    Integer element = Integer.valueOf( 0 );
                    final Iterator<Integer> itr = rankedList.iterator();
                    for ( int i = 0; i < random.nextInt( rankedList.size() + 1 ) && itr.hasNext(); i++ )
                    {
                        element = itr.next();
                    }
                    rankedList.remove( element );
                    Thread.yield();
                }
            }
            catch ( final Throwable e )
            {
                errors.add( e );
            }
        }
    }

    static class Read
        implements Runnable
    {
        public void run()
        {
            try
            {
                while ( active.get() )
                {
                    Thread.yield();
                    int lastRank = Integer.MAX_VALUE;
                    final Iterator<Integer> itr = rankedList.iterator();
                    while ( itr.hasNext() )
                    {
                        Thread.yield();
                        final int rank = itr.next().intValue();
                        Thread.yield();
                        assertTrue( "Rank should descend during iteration", lastRank >= rank );
                        lastRank = rank;
                    }
                    Thread.yield();
                }
            }
            catch ( final Throwable e )
            {
                errors.add( e );
            }
        }
    }
}
