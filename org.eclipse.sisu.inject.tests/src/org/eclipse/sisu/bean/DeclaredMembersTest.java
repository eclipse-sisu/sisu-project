/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.bean;

import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category( org.eclipse.sisu.BaseTests.class )
public class DeclaredMembersTest
    extends TestCase
{
    interface A
    {
        char a = 'a';

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
        final Iterator<Member> i = new DeclaredMembers( null ).iterator();

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

    public void testJavaClass()
    {
        final Iterator<Member> i = new DeclaredMembers( Object.class ).iterator();

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
        final Iterator<Member> i = new DeclaredMembers( D.class ).iterator();

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testInterfaceHierarchy()
        throws NoSuchMethodException, NoSuchFieldException
    {
        final Member[] elements = { C.class.getDeclaredMethod( "c" ), C.class.getDeclaredField( "c" ) };

        int i = 0;
        for ( final Member e : new DeclaredMembers( C.class ) )
        {
            assertEquals( elements[i++], e );
        }
        assertEquals( 2, i );
    }

    public void testClassHierarchy()
        throws NoSuchMethodException, NoSuchFieldException
    {
        final Member[] elements =
            { D.class.getDeclaredConstructor(), D.class.getDeclaredMethod( "c" ), D.class.getDeclaredField( "d" ),
                B.class.getDeclaredConstructor(), B.class.getDeclaredMethod( "a" ), B.class.getDeclaredField( "b" ) };

        int i = 0;
        for ( final Member e : new DeclaredMembers( D.class ) )
        {
            if ( !e.getName().startsWith( "$" ) )
            {
                assertEquals( elements[i++], e );
            }
        }
        assertEquals( 6, i );
    }

    public void testResumableIteration()
        throws ClassNotFoundException
    {
        final Iterator<Member> itr = new DeclaredMembers( Class.forName( "Incomplete" ) ).iterator();
        assertTrue( itr.hasNext() );
        assertEquals( "public Incomplete(java.lang.String)", itr.next().toString() );
        try
        {
            itr.hasNext();
            fail( "Expected NoClassDefFoundError" );
        }
        catch ( final NoClassDefFoundError e )
        {
            assertEquals( "java.lang.NoClassDefFoundError: Param", e.toString() );
        }
        assertTrue( itr.hasNext() );
        assertEquals( "public java.lang.String Incomplete.address", itr.next().toString() );
        try
        {
            itr.hasNext();
            fail( "Expected NoClassDefFoundError" );
        }
        catch ( final NoClassDefFoundError e )
        {
            assertEquals( "java.lang.NoClassDefFoundError: Param", e.toString() );
        }
        assertTrue( itr.hasNext() );
        assertEquals( "public void IncompleteBase.setName(java.lang.String)", itr.next().toString() );
        assertFalse( itr.hasNext() );
    }
}
