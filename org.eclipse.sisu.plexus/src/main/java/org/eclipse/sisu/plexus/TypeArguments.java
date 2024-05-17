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
package org.eclipse.sisu.plexus;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Utility methods for dealing with generic type arguments.
 */
public final class TypeArguments
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Type OBJECT_TYPE = Object.class;

    private static final Type[] NO_TYPES = {};

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private TypeArguments()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Get all type arguments from a generic type, for example {@code [Foo,Bar]} from {@code Map<Foo,Bar>}.
     * 
     * @param type The generic type
     * @return Array of type arguments
     */
    public static Type[] get( final Type type )
    {
        if ( type instanceof ParameterizedType )
        {
            final Type[] argumentTypes = ( (ParameterizedType) type ).getActualTypeArguments();
            for ( int i = 0; i < argumentTypes.length; i++ )
            {
                argumentTypes[i] = expand( argumentTypes[i] );
            }
            return argumentTypes;
        }
        if ( type instanceof GenericArrayType )
        {
            return new Type[] { expand( ( (GenericArrayType) type ).getGenericComponentType() ) };
        }
        return NO_TYPES;
    }

    /**
     * Get an indexed type argument from a generic type, for example {@code Bar} from {@code Map<Foo,Bar>}.
     * 
     * @param type The generic type
     * @param index The argument index
     * @return Indexed type argument; {@code Object.class} if the given type is a raw class
     */
    public static Type get( final Type type, final int index )
    {
        if ( type instanceof ParameterizedType )
        {
            return expand( ( (ParameterizedType) type ).getActualTypeArguments()[index] );
        }
        if ( type instanceof GenericArrayType )
        {
            if ( 0 == index )
            {
                return expand( ( (GenericArrayType) type ).getGenericComponentType() );
            }
            throw new ArrayIndexOutOfBoundsException( index );
        }
        return OBJECT_TYPE;
    }

    /**
     * Get the erased raw {@link Class} for a generic type, for example {@code Map} from {@code Map<Foo,Bar>}.
     *
     * @param type The generic type
     * @return Erased raw type
     */
    public static Class<?> getRawType( final Type type )
    {
        if ( type instanceof Class<?> )
        {
            return (Class<?>) type;
        }
        if ( type instanceof ParameterizedType )
        {
            return (Class<?>) ( (ParameterizedType) type ).getRawType();
        }
        if ( type instanceof GenericArrayType )
        {
            Class<?> rawComponentType = getRawType( ( (GenericArrayType) type ).getGenericComponentType() );
            return Array.newInstance( rawComponentType, 0 ).getClass();
        }
        return Object.class;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Expands wild-card types where possible, for example {@code Bar} from {@code ? extends Bar}.
     * 
     * @param type The generic type
     * @return Widened type that is still assignment-compatible with the original.
     */
    private static Type expand( final Type type )
    {
        if ( type instanceof WildcardType )
        {
            return ( (WildcardType) type ).getUpperBounds()[0];
        }
        if ( type instanceof TypeVariable<?> )
        {
            return ( (TypeVariable<?>) type ).getBounds()[0];
        }
        return type;
    }
}
