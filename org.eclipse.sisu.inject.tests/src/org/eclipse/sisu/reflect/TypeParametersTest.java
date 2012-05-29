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

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import junit.framework.TestCase;

import com.google.inject.ImplementedBy;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

public class TypeParametersTest
    extends TestCase
{
    static TypeLiteral<Object> OBJECT_TYPE = TypeLiteral.get( Object.class );

    static TypeLiteral<String> STRING_TYPE = TypeLiteral.get( String.class );

    static TypeLiteral<Float> FLOAT_TYPE = TypeLiteral.get( Float.class );

    static TypeLiteral<Short> SHORT_TYPE = TypeLiteral.get( Short.class );

    static TypeLiteral<Number> NUMBER_TYPE = TypeLiteral.get( Number.class );

    @SuppressWarnings( "rawtypes" )
    List rawList;

    List<Short> shortList;

    List<?> wildcardList;

    List<? extends String> wildcardStringList;

    @SuppressWarnings( "rawtypes" )
    Map rawMap;

    Map<String, Float> stringFloatMap;

    Map<?, ?> wildcardMap;

    Map<? extends Float, ? extends Short> wildcardFloatShortMap;

    interface CallableNumber<T extends Number>
        extends Callable<T>
    {
    }

    public void testTypeArguments()
    {
        TypeLiteral<?>[] types;

        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "rawList" ), 0 ) );
        types = TypeParameters.get( getFieldType( "rawList" ) );
        assertEquals( 0, types.length );

        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "rawMap" ), 0 ) );
        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "rawMap" ), 1 ) );
        types = TypeParameters.get( getFieldType( "rawMap" ) );
        assertEquals( 0, types.length );

        assertEquals( SHORT_TYPE, TypeParameters.get( getFieldType( "shortList" ), 0 ) );
        types = TypeParameters.get( getFieldType( "shortList" ) );
        assertEquals( 1, types.length );
        assertEquals( SHORT_TYPE, types[0] );

        assertEquals( STRING_TYPE, TypeParameters.get( getFieldType( "stringFloatMap" ), 0 ) );
        assertEquals( FLOAT_TYPE, TypeParameters.get( getFieldType( "stringFloatMap" ), 1 ) );
        types = TypeParameters.get( getFieldType( "stringFloatMap" ) );
        assertEquals( 2, types.length );
        assertEquals( STRING_TYPE, types[0] );
        assertEquals( FLOAT_TYPE, types[1] );

        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "wildcardList" ), 0 ) );
        types = TypeParameters.get( getFieldType( "wildcardList" ) );
        assertEquals( 1, types.length );
        assertEquals( OBJECT_TYPE, types[0] );

        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "wildcardMap" ), 0 ) );
        assertEquals( OBJECT_TYPE, TypeParameters.get( getFieldType( "wildcardMap" ), 1 ) );
        types = TypeParameters.get( getFieldType( "wildcardMap" ) );
        assertEquals( 2, types.length );
        assertEquals( OBJECT_TYPE, types[0] );
        assertEquals( OBJECT_TYPE, types[1] );

        assertEquals( STRING_TYPE, TypeParameters.get( getFieldType( "wildcardStringList" ), 0 ) );
        types = TypeParameters.get( getFieldType( "wildcardStringList" ) );
        assertEquals( 1, types.length );
        assertEquals( STRING_TYPE, types[0] );

        assertEquals( FLOAT_TYPE, TypeParameters.get( getFieldType( "wildcardFloatShortMap" ), 0 ) );
        assertEquals( SHORT_TYPE, TypeParameters.get( getFieldType( "wildcardFloatShortMap" ), 1 ) );
        types = TypeParameters.get( getFieldType( "wildcardFloatShortMap" ) );
        assertEquals( 2, types.length );
        assertEquals( FLOAT_TYPE, types[0] );
        assertEquals( SHORT_TYPE, types[1] );

        final TypeLiteral<?> genericSuperType = TypeLiteral.get( CallableNumber.class ).getSupertype( Callable.class );

        assertEquals( NUMBER_TYPE, TypeParameters.get( genericSuperType, 0 ) );
        types = TypeParameters.get( genericSuperType );
        assertEquals( 1, types.length );
        assertEquals( NUMBER_TYPE, types[0] );
    }

    @SuppressWarnings( "rawtypes" )
    List[] rawListArray;

    List<Short>[] shortListArray;

    List<?>[] wildcardListArray;

    List<? extends String>[] wildcardStringListArray;

    @SuppressWarnings( "rawtypes" )
    Map[] rawMapArray;

    Map<String, Float>[] stringFloatMapArray;

    Map<?, ?>[] wildcardMapArray;

    Map<? extends Float, ? extends Short>[] wildcardFloatShortMapArray;

    List<String[]> stringArrayList;

    public void testComponentType()
    {
        TypeLiteral<?>[] types;

        types = TypeParameters.get( getFieldType( "rawListArray" ) );
        assertEquals( getFieldType( "rawList" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "rawListArray" ), 0 ) );
        assertEquals( List.class, types[0].getType() );

        types = TypeParameters.get( getFieldType( "rawMapArray" ) );
        assertEquals( getFieldType( "rawMap" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "rawMapArray" ), 0 ) );
        assertEquals( Map.class, types[0].getType() );

        types = TypeParameters.get( getFieldType( "shortListArray" ) );
        assertEquals( getFieldType( "shortList" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "shortListArray" ), 0 ) );
        assertEquals( Types.listOf( Short.class ), types[0].getType() );

        types = TypeParameters.get( getFieldType( "stringFloatMapArray" ) );
        assertEquals( getFieldType( "stringFloatMap" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "stringFloatMapArray" ), 0 ) );
        assertEquals( Types.mapOf( String.class, Float.class ), types[0].getType() );

        types = TypeParameters.get( getFieldType( "wildcardListArray" ) );
        assertEquals( getFieldType( "wildcardList" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "wildcardListArray" ), 0 ) );
        assertEquals( Types.listOf( Types.subtypeOf( Object.class ) ), types[0].getType() );

        types = TypeParameters.get( getFieldType( "wildcardMapArray" ) );
        assertEquals( getFieldType( "wildcardMap" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "wildcardMapArray" ), 0 ) );
        assertEquals( Types.mapOf( Types.subtypeOf( Object.class ), Types.subtypeOf( Object.class ) ),
                      types[0].getType() );

        types = TypeParameters.get( getFieldType( "wildcardStringListArray" ) );
        assertEquals( getFieldType( "wildcardStringList" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "wildcardStringListArray" ), 0 ) );
        assertEquals( Types.listOf( Types.subtypeOf( String.class ) ), types[0].getType() );

        types = TypeParameters.get( getFieldType( "wildcardFloatShortMapArray" ) );
        assertEquals( getFieldType( "wildcardFloatShortMap" ), types[0] );
        assertEquals( types[0], TypeParameters.get( getFieldType( "wildcardFloatShortMapArray" ), 0 ) );
        assertEquals( Types.mapOf( Types.subtypeOf( Float.class ), Types.subtypeOf( Short.class ) ), types[0].getType() );

        types = TypeParameters.get( TypeParameters.get( getFieldType( "stringArrayList" ) )[0] );
        assertEquals( STRING_TYPE, types[0] );
        assertEquals( types[0], TypeParameters.get( TypeParameters.get( getFieldType( "stringArrayList" ), 0 ), 0 ) );
    }

    public void testTypeParameterRangeChecks()
    {
        try
        {
            TypeParameters.get( getFieldType( "stringFloatMap" ), -1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }
        try
        {
            TypeParameters.get( getFieldType( "stringFloatMap" ), 2 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }

        try
        {
            TypeParameters.get( getFieldType( "wildcardStringListArray" ), -1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }
        try
        {
            TypeParameters.get( getFieldType( "wildcardStringListArray" ), 1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
        }
    }

    static class CallableImpl<T>
        implements Callable<T>
    {
        public T call()
            throws Exception
        {
            return null;
        }
    }

    static class CallableNumberImpl<T extends Number>
        implements CallableNumber<T>
    {
        public T call()
            throws Exception
        {
            return null;
        }
    }

    @SuppressWarnings( "rawtypes" )
    static class CallableListImpl
        implements Callable<List>
    {
        public List call()
            throws Exception
        {
            return null;
        }
    }

    @SuppressWarnings( "rawtypes" )
    public void testIsAssignableFrom()
    {
        // === simple types ===

        assertTrue( TypeParameters.isAssignableFrom( TypeLiteral.get( Object.class ), TypeLiteral.get( String.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( TypeLiteral.get( Number.class ), TypeLiteral.get( Short.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( TypeLiteral.get( Collection.class ), TypeLiteral.get( Set.class ) ) );

        // === generic types ===

        assertFalse( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<Collection>>()
        {
        }, TypeLiteral.get( CallableListImpl.class ) ) ); // not assignable since no wild-card
        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<List>>()
        {
        }, TypeLiteral.get( CallableListImpl.class ) ) );
        assertFalse( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<String>>()
        {
        }, TypeLiteral.get( CallableListImpl.class ) ) );

        // === unbound type-variables ===

        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<Callable>()
        {
        }, TypeLiteral.get( Callable.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<Callable>()
        {
        }, TypeLiteral.get( CallableImpl.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<String>>()
        {
        }, TypeLiteral.get( CallableImpl.class ) ) );
        assertFalse( TypeParameters.isAssignableFrom( new TypeLiteral<CallableImpl>()
        {
        }, TypeLiteral.get( Callable.class ) ) );

        // === bound type-variables ===

        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<CallableNumber>()
        {
        }, TypeLiteral.get( CallableNumberImpl.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<CallableNumber<Number>>()
        {
        }, TypeLiteral.get( CallableNumberImpl.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<CallableNumber<Float>>()
        {
        }, TypeLiteral.get( CallableNumberImpl.class ) ) );
        assertFalse( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<String>>()
        {
        }, TypeLiteral.get( CallableNumberImpl.class ) ) ); // mismatched type-bounds

        // === unbound wild-cards ===

        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<?>>()
        {
        }, TypeLiteral.get( CallableImpl.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<?>>()
        {
        }, TypeLiteral.get( CallableNumberImpl.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<CallableNumber<?>>()
        {
        }, TypeLiteral.get( CallableNumberImpl.class ) ) );

        // === bound wild-cards ===

        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<? extends Collection>>()
        {
        }, TypeLiteral.get( CallableListImpl.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<? extends Number>>()
        {
        }, TypeLiteral.get( CallableNumberImpl.class ) ) );
        assertTrue( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<? extends Float>>()
        {
        }, TypeLiteral.get( CallableNumberImpl.class ) ) );
        assertFalse( TypeParameters.isAssignableFrom( new TypeLiteral<Callable<? extends String>>()
        {
        }, TypeLiteral.get( CallableNumberImpl.class ) ) );

        // === array types ===

        assertTrue( TypeParameters.isAssignableFrom( TypeLiteral.get( Types.arrayOf( Object.class ) ),
                                                     TypeLiteral.get( Types.arrayOf( String.class ) ) ) );
        assertTrue( TypeParameters.isAssignableFrom( TypeLiteral.get( Types.arrayOf( Number.class ) ),
                                                     TypeLiteral.get( Types.arrayOf( Float.class ) ) ) );

        // === mismatched types ===

        assertFalse( TypeParameters.isAssignableFrom( TypeLiteral.get( Types.arrayOf( Object.class ) ),
                                                      TypeLiteral.get( Types.listOf( Object.class ) ) ) );
        assertFalse( TypeParameters.isAssignableFrom( TypeLiteral.get( Types.listOf( Object.class ) ),
                                                      TypeLiteral.get( Types.arrayOf( Object.class ) ) ) );

        // === corner case ===

        final Type T = new TypeVariable()
        {
            public Type[] getBounds()
            {
                return new Type[] { String.class };
            }

            public GenericDeclaration getGenericDeclaration()
            {
                return null;
            }

            public String getName()
            {
                return "T";
            }
        };

        final Type callableT = new ParameterizedType()
        {
            public Type getRawType()
            {
                return Callable.class;
            }

            public Type getOwnerType()
            {
                return null;
            }

            public Type[] getActualTypeArguments()
            {
                return new Type[] { T };
            }
        };

        assertFalse( TypeParameters.isAssignableFrom( TypeLiteral.get( callableT ), TypeLiteral.get( Callable.class ) ) );

        assertFalse( TypeParameters.isAssignableFrom( TypeLiteral.get( callableT ),
                                                      TypeLiteral.get( CallableNumberImpl.class ) ) );
    }

    public void testIsConcrete()
    {
        assertFalse( TypeParameters.isConcrete( Map.class ) );
        assertFalse( TypeParameters.isConcrete( AbstractMap.class ) );
        assertTrue( TypeParameters.isConcrete( HashMap.class ) );

        assertFalse( TypeParameters.isConcrete( new TypeLiteral<Map<String, String>>()
        {
        } ) );
        assertFalse( TypeParameters.isConcrete( new TypeLiteral<AbstractMap<String, String>>()
        {
        } ) );
        assertTrue( TypeParameters.isConcrete( new TypeLiteral<HashMap<String, String>>()
        {
        } ) );
    }

    @ImplementedBy( Object.class )
    static interface Implicit1<T>
    {
    }

    static class SomeProvider
        implements Provider<Object>
    {
        public Object get()
        {
            return null;
        }
    }

    @ProvidedBy( SomeProvider.class )
    static interface Implicit2<T>
    {
    }

    public void testIsImplicit()
    {
        assertFalse( TypeParameters.isImplicit( Map.class ) );
        assertFalse( TypeParameters.isImplicit( AbstractMap.class ) );
        assertTrue( TypeParameters.isImplicit( HashMap.class ) );

        assertFalse( TypeParameters.isImplicit( new TypeLiteral<Map<String, String>>()
        {
        } ) );
        assertFalse( TypeParameters.isImplicit( new TypeLiteral<AbstractMap<String, String>>()
        {
        } ) );
        assertTrue( TypeParameters.isImplicit( new TypeLiteral<HashMap<String, String>>()
        {
        } ) );

        assertTrue( TypeParameters.isImplicit( Implicit1.class ) );
        assertTrue( TypeParameters.isImplicit( Implicit2.class ) );

        assertTrue( TypeParameters.isImplicit( new TypeLiteral<Implicit1<String>>()
        {
        } ) );
        assertTrue( TypeParameters.isImplicit( new TypeLiteral<Implicit2<String>>()
        {
        } ) );
    }

    private static TypeLiteral<?> getFieldType( final String name )
    {
        try
        {
            return TypeLiteral.get( TypeParametersTest.class.getDeclaredField( name ).getGenericType() );
        }
        catch ( final NoSuchFieldException e )
        {
            throw new IllegalArgumentException( "Unknown test field " + name );
        }
    }
}
