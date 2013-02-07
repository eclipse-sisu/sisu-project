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

import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class PublicMembersTest
    extends TestCase
{
    interface A
    {
        void a();
    }

    static class B
        implements A
    {
        public B()
        {
        }

        char b = 'b';

        public void a()
        {
        }
    }

    interface C
        extends A
    {
        char c = 'c';

        void c();
    }

    static class D
        extends B
        implements C
    {
        public D()
        {
        }

        char d = 'd';

        public void c()
        {
        }
    }

    public void testNullClass()
    {
        final Iterator<Member> i = new PublicMembers( null ).iterator();

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

    public void testReadOnlyIterator()
    {
        final Iterator<Member> i = new PublicMembers( D.class ).iterator();

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testInterface()
        throws NoSuchMethodException, NoSuchFieldException
    {
        final Member[] elements = { C.class.getMethod( "a" ), C.class.getMethod( "c" ), C.class.getField( "c" ) };

        int i = 0;
        for ( final Member e : new PublicMembers( C.class ) )
        {
            assertEquals( elements[i++], e );
        }
        assertEquals( 3, i );
    }

    public void testClass()
        throws NoSuchMethodException, NoSuchFieldException
    {
        /* note interface field 'a' appears twice due to multi-interface inheritance */
        final Member[] elements =
            { D.class.getConstructor(), D.class.getMethod( "a" ), D.class.getMethod( "c" ), D.class.getField( "c" ) };

        int i = 0;
        for ( final Member e : new PublicMembers( D.class ) )
        {
            if ( e.getDeclaringClass() == Object.class )
            {
                continue; // ignore java.lang.Object members as they vary according to JDK
            }
            assertEquals( elements[i++], e );
        }
        assertEquals( 4, i );
    }
}
