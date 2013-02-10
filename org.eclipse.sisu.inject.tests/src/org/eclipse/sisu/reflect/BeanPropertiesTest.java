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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Named;
import javax.inject.Singleton;

import junit.framework.TestCase;

import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

@SuppressWarnings( "unused" )
public class BeanPropertiesTest
    extends TestCase
{
    @Retention( RetentionPolicy.RUNTIME )
    @interface Metadata
    {
        String value();
    }

    @Target( ElementType.METHOD )
    @Retention( RetentionPolicy.RUNTIME )
    @interface MethodMetadata
    {
        String value();
    }

    @Target( ElementType.FIELD )
    @Retention( RetentionPolicy.RUNTIME )
    @interface FieldMetadata
    {
        String value();
    }

    @Target( { ElementType.FIELD, ElementType.METHOD } )
    @Retention( RetentionPolicy.RUNTIME )
    @interface MultiMetadata
    {
        String value();
    }

    static interface A
    {
        String name = "";

        void setName( String name );
    }

    static class B
    {
        static void setName( final String name )
        {
        }
    }

    static class C
    {
        final String name = "";
    }

    static class D
    {
        public D()
        {
        }

        private void setName( final String name )
        {
        }
    }

    static class E
    {
        void setName()
        {
        }

        void setName( final String firstName, final String lastName )
        {
        }

        void name( final String _name )
        {
        }
    }

    static class F
    {
        void setName( final String name )
        {
        }

        void setName()
        {
        }

        String name;

        void setName( final String firstName, final String lastName )
        {
        }

        void name( final String _name )
        {
        }
    }

    static class G
    {
        List<String> names;

        void setMap( final Map<BigDecimal, Float> map )
        {
        }
    }

    static abstract class IBase<T>
    {
        public abstract void setId( T id );
    }

    static class H
        extends IBase<String>
    {
        private volatile String vid = "test";

        private static Internal internal = new Internal();

        static class Internal
        {
            String m_id;
        }

        @Override
        public void setId( final String _id )
        {
            internal.m_id = _id;
        }

        @Override
        public String toString()
        {
            return vid + "@" + internal.m_id;
        }
    }

    static class I
    {
        @Singleton
        @Named( "bar" )
        String bar;

        @Singleton
        @Named( "foo" )
        void setFoo( final String foo )
        {
        }
    }

    static class J
    {
        String a;

        String b;

        String c;
    }

    static class K
    {
        void setName( final String name )
        {
            throw new RuntimeException();
        }
    }

    static class L
    {
        void setter( final String value )
        {
        }
    }

    static class M
    {
        void set( final String value )
        {
        }
    }

    static class N
    {
        @Metadata( "Field1" )
        @MultiMetadata( "A" )
        @FieldMetadata( "1" )
        int value;

        @MethodMetadata( "1" )
        void init()
        {
        }

        @MethodMetadata( "2" )
        void setValue( final int value )
        {
            this.value = value;
        }
    }

    static class O1
    {
        private String a1;

        void setA( final String a )
        {
        }
    }

    @IgnoreSetters
    static class O2
        extends O1
    {
        private String b2;

        void setB( final String b )
        {
        }
    }

    static class O3
        extends O2
    {
        void setC( final String c )
        {
        }
    }

    public void testInterface()
    {
        for ( final BeanProperty<?> bp : new BeanProperties( A.class ) )
        {
            fail( "Expected no bean properties" );
        }
    }

    public void testEmptyClass()
    {
        for ( final BeanProperty<?> bp : new BeanProperties( B.class ) )
        {
            fail( "Expected no bean properties" );
        }
    }

    public void testPropertyField()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( C.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );
    }

    public void testPropertySetter()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( D.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );
    }

    public void testHashCodeAndEquals()
        throws Exception
    {
        final BeanProperty<Object> propertyField = new BeanProperties( C.class ).iterator().next();
        final BeanProperty<Object> propertySetter = new BeanProperties( D.class ).iterator().next();

        assertEquals( propertyField, propertyField );
        assertEquals( propertySetter, propertySetter );

        assertFalse( propertyField.equals( propertySetter ) );
        assertFalse( propertySetter.equals( propertyField ) );

        final Field field = C.class.getDeclaredField( "name" );
        final Method setter = D.class.getDeclaredMethod( "setName", String.class );

        assertEquals( propertyField, new BeanPropertyField<Object>( field ) );
        assertEquals( propertySetter, new BeanPropertySetter<Object>( setter ) );

        assertFalse( propertyField.equals( new BeanPropertyField<Object>( F.class.getDeclaredField( "name" ) ) ) );
        assertFalse( propertySetter.equals( new BeanPropertySetter<Object>( F.class.getDeclaredMethod( "setName",
                                                                                                       String.class ) ) ) );

        assertEquals( field.hashCode(), propertyField.hashCode() );
        assertEquals( setter.hashCode(), propertySetter.hashCode() );
        assertEquals( field.toString(), propertyField.toString() );
        assertEquals( setter.toString(), propertySetter.toString() );
    }

    public void testSkipInvalidSetters()
    {
        for ( final BeanProperty<?> bp : new BeanProperties( E.class ) )
        {
            fail( "Expected no bean properties" );
        }
    }

    public void testPropertyCombination()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( F.class ).iterator();
        BeanProperty<Object> bp;

        bp = i.next();
        assertEquals( "name", bp.getName() );
        assertTrue( bp instanceof BeanPropertySetter<?> );
        bp = i.next();
        assertEquals( "name", bp.getName() );
        assertTrue( bp instanceof BeanPropertyField<?> );
        assertFalse( i.hasNext() );

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
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testConstructor()
        throws NoSuchMethodException
    {
        final Iterable<Member> members = Collections.singleton( (Member) String.class.getConstructor() );
        final Iterator<BeanProperty<Object>> i = new BeanProperties( members ).iterator();
        assertFalse( i.hasNext() );
    }

    public void testPropertyType()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( G.class ).iterator();
        assertEquals( TypeLiteral.get( Types.mapOf( BigDecimal.class, Float.class ) ), i.next().getType() );
        assertEquals( TypeLiteral.get( Types.listOf( String.class ) ), i.next().getType() );
    }

    public void testPropertyUpdate()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( H.class ).iterator();
        final BeanProperty<Object> a = i.next();
        final BeanProperty<Object> b = i.next();
        assertFalse( i.hasNext() );

        final H component = new H();

        a.set( component, "bar" );
        b.set( component, "foo" );

        assertEquals( "foo@bar", component.toString() );

        b.set( component, "abc" );
        a.set( component, "xyz" );

        assertEquals( "abc@xyz", component.toString() );
    }

    public void testIllegalAccess()
    {
        try
        {
            final BeanProperty<Object> p = new BeanPropertyField<Object>( A.class.getDeclaredField( "name" ) );
            p.set( new Object(), "test" );
            fail( "Expected RuntimeException" );
        }
        catch ( final NoSuchFieldException e )
        {
            fail( e.toString() );
        }
        catch ( final RuntimeException e )
        {
            e.printStackTrace();
        }

        try
        {
            final BeanProperty<Object> p =
                new BeanPropertySetter<Object>( A.class.getDeclaredMethod( "setName", String.class ) );
            p.set( new Object(), "test" );
            fail( "Expected RuntimeException" );
        }
        catch ( final NoSuchMethodException e )
        {
            fail( e.toString() );
        }
        catch ( final RuntimeException e )
        {
            e.printStackTrace();
        }
    }

    public void testPropertyAnnotations()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( I.class ).iterator();
        assertEquals( "foo", i.next().getAnnotation( Named.class ).value() );
        assertEquals( "bar", i.next().getAnnotation( Named.class ).value() );
        assertFalse( i.hasNext() );
    }

    public void testPropertyIteration()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( J.class ).iterator();
        assertTrue( i.hasNext() );
        assertTrue( i.hasNext() );
        assertEquals( "c", i.next().getName() );
        assertTrue( i.hasNext() );
        assertTrue( i.hasNext() );
        assertEquals( "b", i.next().getName() );
        assertTrue( i.hasNext() );
        assertTrue( i.hasNext() );
        assertEquals( "a", i.next().getName() );
        assertFalse( i.hasNext() );
        assertFalse( i.hasNext() );
    }

    public void testBadPropertySetter()
    {
        try
        {
            final Iterator<BeanProperty<Object>> i = new BeanProperties( K.class ).iterator();
            i.next().set( new K(), "TEST" );
            fail( "Expected RuntimeException" );
        }
        catch ( final RuntimeException e )
        {
            e.printStackTrace();
        }
    }

    public void testSetterNames()
    {
        assertFalse( new BeanProperties( L.class ).iterator().hasNext() );
        assertFalse( new BeanProperties( M.class ).iterator().hasNext() );
    }

    public void testIgnoreSetters()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( O3.class ).iterator();

        assertTrue( i.hasNext() );
        assertEquals( "b2", i.next().getName() );
        assertTrue( i.hasNext() );
        assertEquals( "a1", i.next().getName() );
        assertFalse( i.hasNext() );
    }
}
