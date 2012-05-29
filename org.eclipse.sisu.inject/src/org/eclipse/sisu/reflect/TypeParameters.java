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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import com.google.inject.ImplementedBy;
import com.google.inject.ProvidedBy;
import com.google.inject.TypeLiteral;

/**
 * Utility methods for dealing with generic type parameters and arguments.
 */
public final class TypeParameters
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final TypeLiteral<?>[] NO_TYPE_LITERALS = {};

    private static final TypeLiteral<?> OBJECT_TYPE_LITERAL = TypeLiteral.get( Object.class );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private TypeParameters()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Get all type arguments from a generic type, for example {@code [Foo,Bar]} from {@code Map<Foo,Bar>}.
     * 
     * @param typeLiteral The generic type
     * @return Array of type arguments
     */
    public static TypeLiteral<?>[] get( final TypeLiteral<?> typeLiteral )
    {
        final Type type = typeLiteral.getType();
        if ( type instanceof ParameterizedType )
        {
            final Type[] argumentTypes = ( (ParameterizedType) type ).getActualTypeArguments();
            final TypeLiteral<?>[] argumentLiterals = new TypeLiteral[argumentTypes.length];
            for ( int i = 0; i < argumentTypes.length; i++ )
            {
                argumentLiterals[i] = expand( argumentTypes[i] );
            }
            return argumentLiterals;
        }
        if ( type instanceof GenericArrayType )
        {
            return new TypeLiteral[] { expand( ( (GenericArrayType) type ).getGenericComponentType() ) };
        }
        return NO_TYPE_LITERALS;
    }

    /**
     * Get an indexed type argument from a generic type, for example {@code Bar} from {@code Map<Foo,Bar>}.
     * 
     * @param typeLiteral The generic type
     * @param index The argument index
     * @return Indexed type argument; {@code TypeLiteral<Object>} if the given type is a raw class
     */
    public static TypeLiteral<?> get( final TypeLiteral<?> typeLiteral, final int index )
    {
        final Type type = typeLiteral.getType();
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
        return OBJECT_TYPE_LITERAL;
    }

    /**
     * Determines if the sub-type can be converted to the generic super-type via an identity or widening conversion.
     * 
     * @param superLiteral The generic super-type
     * @param subLiteral The generic sub-type
     * @return {@code true} if the sub-type can be converted to the generic super-type; otherwise {@code false}
     * @see Class#isAssignableFrom(Class)
     */
    public static boolean isAssignableFrom( final TypeLiteral<?> superLiteral, final TypeLiteral<?> subLiteral )
    {
        final Class<?> superClazz = superLiteral.getRawType();
        if ( !superClazz.isAssignableFrom( subLiteral.getRawType() ) )
        {
            return false;
        }
        final Type superType = superLiteral.getType();
        if ( superClazz == superType )
        {
            return true;
        }
        if ( superType instanceof ParameterizedType )
        {
            final Type resolvedType = subLiteral.getSupertype( superClazz ).getType();
            if ( resolvedType instanceof ParameterizedType )
            {
                final Type[] superArgs = ( (ParameterizedType) superType ).getActualTypeArguments();
                final Type[] subArgs = ( (ParameterizedType) resolvedType ).getActualTypeArguments();
                return isAssignableFrom( superArgs, subArgs );
            }
        }
        else if ( superType instanceof GenericArrayType )
        {
            final Type resolvedType = subLiteral.getSupertype( superClazz ).getType();
            if ( resolvedType instanceof GenericArrayType )
            {
                final Type superComponent = ( (GenericArrayType) superType ).getGenericComponentType();
                final Type subComponent = ( (GenericArrayType) resolvedType ).getGenericComponentType();
                return isAssignableFrom( new Type[] { superComponent }, new Type[] { subComponent } );
            }
        }
        return false;
    }

    /**
     * Determines if the given generic type represents a concrete type.
     * 
     * @param literal The generic type
     * @return {@code true} if the generic type is concrete; otherwise {@code false}
     */
    public static boolean isConcrete( final TypeLiteral<?> literal )
    {
        return isConcrete( literal.getRawType() );
    }

    /**
     * Determines if the given raw type represents a concrete type.
     * 
     * @param clazz The raw type
     * @return {@code true} if the raw type is concrete; otherwise {@code false}
     */
    public static boolean isConcrete( final Class<?> clazz )
    {
        return !clazz.isInterface() && !Modifier.isAbstract( clazz.getModifiers() );
    }

    /**
     * Determines if the given generic type represents an implicit binding.
     * 
     * @param literal The generic type
     * @return {@code true} if the generic type is implicit; otherwise {@code false}
     */
    public static boolean isImplicit( final TypeLiteral<?> literal )
    {
        return isImplicit( literal.getRawType() );
    }

    /**
     * Determines if the given raw type represents an implicit binding.
     * 
     * @param clazz The raw type
     * @return {@code true} if the raw type is implicit; otherwise {@code false}
     */
    public static boolean isImplicit( final Class<?> clazz )
    {
        return isConcrete( clazz ) || clazz.isAnnotationPresent( ImplementedBy.class )
            || clazz.isAnnotationPresent( ProvidedBy.class );
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
    private static TypeLiteral<?> expand( final Type type )
    {
        if ( type instanceof WildcardType )
        {
            return TypeLiteral.get( ( (WildcardType) type ).getUpperBounds()[0] );
        }
        if ( type instanceof TypeVariable<?> )
        {
            return TypeLiteral.get( ( (TypeVariable<?>) type ).getBounds()[0] );
        }
        return TypeLiteral.get( type );
    }

    /**
     * Determines whether the resolved sub-type arguments can be assigned to their generic super-type arguments.
     * 
     * @param superArgs The generic super-arguments
     * @param subArgs The resolved sub-arguments
     * @return {@code true} if all the super-arguments have assignable resolved arguments; otherwise {@code false}
     */
    private static boolean isAssignableFrom( final Type[] superArgs, final Type[] subArgs )
    {
        for ( int i = 0, len = Math.min( superArgs.length, subArgs.length ); i < len; i++ )
        {
            final Type superType = superArgs[i];
            final Type subType = subArgs[i];

            /*
             * Implementations could have unbound type variables, such as ArrayList<T>. We want to support injecting
             * MyList<T extends Number> into List<Double>. This is why the isAssignableFrom parameters are reversed.
             */
            if ( subType instanceof TypeVariable<?> && isAssignableFrom( expand( subType ), expand( superType ) ) )
            {
                continue;
            }
            /*
             * Interfaces can have wild-card types, such as List<? extends Number>. Note: we only check the initial
             * upper-bound of the super-type against the resolved type (trading absolute accuracy for performance).
             */
            if ( superType instanceof WildcardType || superType instanceof TypeVariable<?> )
            {
                if ( !isAssignableFrom( expand( superType ), expand( subType ) ) )
                {
                    return false;
                }
            }
            /*
             * Non-wild-card arguments must be tested with equals: List<Number> is not assignable from List<Float>.
             */
            else if ( !superType.equals( subType ) )
            {
                return false;
            }
        }
        return true;
    }
}
